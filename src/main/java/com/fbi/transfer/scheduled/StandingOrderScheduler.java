package com.fbi.transfer.scheduled;

import com.fbi.transfer.dto.TransferRequest;
import com.fbi.transfer.service.TransferService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.DependsOn;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.Executor;

@Component
@DependsOn("liquibase")
public class StandingOrderScheduler {

    private static final Logger LOG =
            LoggerFactory.getLogger(StandingOrderScheduler.class);

    private final JdbcTemplate jdbcTemplate;
    private final TransferService transferService;
    private final Executor executor;

    public StandingOrderScheduler(
            JdbcTemplate jdbcTemplate,
            TransferService transferService,
            @Qualifier("virtualThreadExecutor") Executor executor
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.transferService = transferService;
        this.executor = executor;
    }

    @Scheduled(cron = "${standing-order.scheduler.cron:0 */1 * * * *}")
    public void executePendingOrders() {

        LOG.info("Starting standing order execution cycle");

        var orders = jdbcTemplate.query(
                """
                SELECT id, from_iban, to_iban, amount
                FROM ACCOUNT_TRANSFER.standing_order
                WHERE status = 'PENDING'
                  AND execution_date <= CURRENT_DATE
                """,
                (rs, rowNum) -> new StandingOrderJob(
                        rs.getLong("id"),
                        rs.getString("from_iban"),
                        rs.getString("to_iban"),
                        rs.getBigDecimal("amount")
                )
        );

        LOG.info("Found {} pending standing orders", orders.size());

        for (StandingOrderJob order : orders) {

            executor.execute(() -> processOrder(order));
        }
    }

    private void processOrder(StandingOrderJob order) {

        LOG.info(
                "Processing standing order: id={}, from={}, to={}, amount={}",
                order.id(),
                order.fromIban(),
                order.toIban(),
                order.amount()
        );

        try {
            String idempotencyKey = "SO-" + UUID.randomUUID();

            TransferRequest request = new TransferRequest(
                    order.fromIban(),
                    order.toIban(),
                    order.amount()
            );

            transferService.transfer(idempotencyKey, request);

            jdbcTemplate.update("""
                UPDATE ACCOUNT_TRANSFER.standing_order
                SET status = 'COMPLETED'
                WHERE id = ?
            """, order.id());

            LOG.info(
                    "Standing order completed successfully: id={}",
                    order.id()
            );
        } catch (Exception ex) {

            jdbcTemplate.update("""
                UPDATE ACCOUNT_TRANSFER.standing_order
                SET status = 'FAILED'
                WHERE id = ?
            """, order.id());

            LOG.error(
                    "Standing order failed: id={}, reason={}",
                    order.id(),
                    ex.getMessage(),
                    ex
            );
        }
    }

    private record StandingOrderJob(
            Long id,
            String fromIban,
            String toIban,
            BigDecimal amount
    ) {}
}