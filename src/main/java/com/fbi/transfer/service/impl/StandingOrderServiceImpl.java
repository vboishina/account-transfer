package com.fbi.transfer.service.impl;

import com.fbi.transfer.dto.StandingOrderRequest;
import com.fbi.transfer.dto.StandingOrderResponse;
import com.fbi.transfer.dto.enums.StandingOrderStatus;
import com.fbi.transfer.service.StandingOrderService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Service
public class StandingOrderServiceImpl implements StandingOrderService {
    private final JdbcTemplate jdbcTemplate;

    public StandingOrderServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public StandingOrderResponse create(StandingOrderRequest request) {

        Instant now = Instant.now();

        var keyHolder = new org.springframework.jdbc.support.GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            var ps = connection.prepareStatement(
                    """
                    INSERT INTO ACCOUNT_TRANSFER.standing_order
                    (from_iban,
                     to_iban,
                     amount,
                     execution_date,
                     status,
                     created_at)
                    VALUES (?, ?, ?, ?, 'PENDING', ?)
                    """,
                    new String[]{"id"}
            );

            ps.setString(1, request.fromIban());
            ps.setString(2, request.toIban());
            ps.setBigDecimal(3, request.amount());
            ps.setDate(4, java.sql.Date.valueOf(request.executionDate()));
            ps.setTimestamp(5, Timestamp.from(now));

            return ps;
        }, keyHolder);

        Long id = keyHolder.getKey().longValue();

        return findById(id);
    }

    @Override
    public List<StandingOrderResponse> findAll() {
        return jdbcTemplate.query(
                """
                SELECT id,
                       from_iban,
                       to_iban,
                       amount,
                       execution_date,
                       status,
                       created_at
                FROM ACCOUNT_TRANSFER.standing_order
                ORDER BY id DESC
                """,
                (rs, rowNum) -> new StandingOrderResponse(
                        rs.getLong("id"),
                        rs.getString("from_iban"),
                        rs.getString("to_iban"),
                        rs.getBigDecimal("amount"),
                        rs.getDate("execution_date").toLocalDate(),
                        StandingOrderStatus.valueOf(rs.getString("status")),
                        rs.getTimestamp("created_at").toInstant()
                )
        );
    }

    @Override
    public StandingOrderResponse findById(Long id) {
        return jdbcTemplate.queryForObject(
                """
                SELECT id,
                       from_iban,
                       to_iban,
                       amount,
                       execution_date,
                       status,
                       created_at
                FROM ACCOUNT_TRANSFER.standing_order
                WHERE id = ?
                """,
                (rs, rowNum) -> new StandingOrderResponse(
                        rs.getLong("id"),
                        rs.getString("from_iban"),
                        rs.getString("to_iban"),
                        rs.getBigDecimal("amount"),
                        rs.getDate("execution_date").toLocalDate(),
                        StandingOrderStatus.valueOf(rs.getString("status")),
                        rs.getTimestamp("created_at").toInstant()
                ),
                id
        );
    }

    @Override
    public void cancel(Long id) {
        jdbcTemplate.update(
                """
                UPDATE ACCOUNT_TRANSFER.standing_order
                SET status = 'CANCELLED'
                WHERE id = ?
                  AND status = 'PENDING'
                """,
                id
        );
    }
}
