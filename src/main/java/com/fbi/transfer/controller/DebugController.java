package com.fbi.transfer.controller;


import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class DebugController {

    private final JdbcTemplate jdbcTemplate;

    public DebugController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/tables")
    public List<Map<String, Object>> tables() {
        return jdbcTemplate.queryForList(
                "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES");
    }

    @GetMapping("/accounts")
    public List<Map<String, Object>> accounts() {
        return jdbcTemplate.queryForList("SELECT * FROM   ACCOUNT_TRANSFER.ACCOUNT");
    }

    @GetMapping("/transfers")
    public List<Map<String, Object>> transfers() {
        return jdbcTemplate.queryForList("SELECT * FROM   ACCOUNT_TRANSFER.TRANSFER");
    }

    @GetMapping("/ledger")
    public List<Map<String, Object>> ledgers() {
        return jdbcTemplate.queryForList("SELECT * FROM   ACCOUNT_TRANSFER.ledger_entry");
    }

    @GetMapping("/idempotency")
    public List<Map<String, Object>> idempotence() {
        return jdbcTemplate.queryForList("SELECT * FROM   ACCOUNT_TRANSFER.idempotency_key");
    }

}