package com.fbi.transfer.repository.jdbc;

import com.fbi.transfer.domain.StandingOrder;
import com.fbi.transfer.repository.StandingOrderRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcStandingOrderRepository
        implements StandingOrderRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcStandingOrderRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Long save(StandingOrder order) {

        GeneratedKeyHolder holder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {

            PreparedStatement ps =
                    connection.prepareStatement(
                            """
                            INSERT INTO STANDING_ORDER
                            (
                                SOURCE_IBAN,
                                DESTINATION_IBAN,
                                AMOUNT,
                                FIRST_EXECUTION_DATE,
                                NEXT_EXECUTION_DATE,
                                EXECUTION_DAY_OF_MONTH,
                                ACTIVE
                            )
                            VALUES (?, ?, ?, ?, ?, ?, ?)
                            """,
                            Statement.RETURN_GENERATED_KEYS
                    );

            ps.setString(1, order.sourceIban());
            ps.setString(2, order.destinationIban());
            ps.setBigDecimal(3, order.amount());
            ps.setDate(4,
                    java.sql.Date.valueOf(order.firstExecutionDate()));
            ps.setDate(5,
                    java.sql.Date.valueOf(order.nextExecutionDate()));
            ps.setInt(6, order.executionDayOfMonth());
            ps.setBoolean(7, order.active());

            return ps;
        }, holder);

        return holder.getKey().longValue();
    }

    @Override
    public List<StandingOrder> findAll() {

        return jdbcTemplate.query(
                """
                SELECT *
                FROM STANDING_ORDER
                WHERE ACTIVE = TRUE
                ORDER BY ID
                """,
                (rs, rowNum) -> new StandingOrder(
                        rs.getLong("ID"),
                        rs.getString("SOURCE_IBAN"),
                        rs.getString("DESTINATION_IBAN"),
                        rs.getBigDecimal("AMOUNT"),
                        rs.getDate("FIRST_EXECUTION_DATE").toLocalDate(),
                        rs.getDate("NEXT_EXECUTION_DATE").toLocalDate(),
                        rs.getInt("EXECUTION_DAY_OF_MONTH"),
                        rs.getBoolean("ACTIVE")
                )
        );
    }

    @Override
    public Optional<StandingOrder> findById(Long id) {

        List<StandingOrder> result = jdbcTemplate.query(
                """
                SELECT *
                FROM STANDING_ORDER
                WHERE ID = ?
                """,
                (rs, rowNum) -> new StandingOrder(
                        rs.getLong("ID"),
                        rs.getString("SOURCE_IBAN"),
                        rs.getString("DESTINATION_IBAN"),
                        rs.getBigDecimal("AMOUNT"),
                        rs.getDate("FIRST_EXECUTION_DATE").toLocalDate(),
                        rs.getDate("NEXT_EXECUTION_DATE").toLocalDate(),
                        rs.getInt("EXECUTION_DAY_OF_MONTH"),
                        rs.getBoolean("ACTIVE")
                ),
                id
        );

        return result.stream().findFirst();
    }

    @Override
    public void deactivate(Long id) {

        jdbcTemplate.update(
                """
                UPDATE STANDING_ORDER
                SET ACTIVE = FALSE
                WHERE ID = ?
                """,
                id
        );
    }
}