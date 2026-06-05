package com.fbi.transfer.service.impl;

import com.fbi.transfer.dto.AccountResponse;
import com.fbi.transfer.service.AccountService;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountServiceImpl implements AccountService {

    private final JdbcTemplate jdbcTemplate;

    public AccountServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<AccountResponse> findAll() {
        return jdbcTemplate.query(
                "SELECT iban, owner, currency, balance FROM ACCOUNT_TRANSFER.ACCOUNT",
                (rs, rowNum) -> new AccountResponse(
                        rs.getString("iban"),
                        rs.getString("owner"),
                        rs.getString("currency"),
                        rs.getBigDecimal("balance")
                )
        );
    }

    @Override
    public AccountResponse findByIban(String iban) {
        String sql = """
                SELECT iban, owner, currency, balance
                FROM ACCOUNT_TRANSFER.account
                WHERE iban = ?
                """;
        return jdbcTemplate.queryForObject(
                sql,
                (rs, rowNum) -> new AccountResponse(
                        rs.getString("iban"),
                        rs.getString("owner"),
                        rs.getString("currency"),
                        rs.getBigDecimal("balance")
                ),
                iban
        );

    }
}
