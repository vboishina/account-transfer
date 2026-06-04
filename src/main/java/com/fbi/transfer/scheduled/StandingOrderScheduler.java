package com.fbi.transfer.scheduled;

import com.fbi.transfer.dto.TransferRequest;
import com.fbi.transfer.service.TransferService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.concurrent.Executor;

@Component
public class StandingOrderScheduler {

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

        System.out.println("SCHEDULED :::::::::");

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

        for (StandingOrderJob order : orders) {

            executor.execute(() -> processOrder(order));
        }
    }

    private void processOrder(StandingOrderJob order) {

        try {
            String idempotencyKey =
                    "SO-" + order.id() + "-" + System.nanoTime();

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

        } catch (Exception ex) {

            jdbcTemplate.update("""
                UPDATE ACCOUNT_TRANSFER.standing_order
                SET status = 'FAILED'
                WHERE id = ?
            """, order.id());

            System.err.println(
                    "FAILED ORDER " + order.id() + ": " + ex.getMessage()
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