package com.fbi.transfer.service.impl;

import com.fbi.transfer.dto.LedgerEntryResponse;
import com.fbi.transfer.service.LedgerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class LedgerServiceImpl implements LedgerService {

    private final JdbcTemplate jdbcTemplate;

    public LedgerServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Page<LedgerEntryResponse> search(String iban, int page, int size) {

        boolean filtered = iban != null && !iban.isBlank();

        String whereClause = filtered
                ? " WHERE iban = ? "
                : "";

        Long total = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*)
                        FROM ACCOUNT_TRANSFER.ledger_entry
                        """ + whereClause,
                Long.class,
                filtered ? iban : null
        );

        String sql = """
                SELECT id,
                       transfer_id,
                       iban,
                       entry_type,
                       amount,
                       currency,
                       created_at
                FROM ACCOUNT_TRANSFER.ledger_entry
                """
                + whereClause +
                """
                        ORDER BY created_at DESC
                        LIMIT ? OFFSET ?
                        """;

        var content = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new LedgerEntryResponse(
                        rs.getLong("id"),
                        rs.getLong("transfer_id"),
                        rs.getString("iban"),
                        rs.getString("entry_type"),
                        rs.getBigDecimal("amount"),
                        rs.getString("currency"),
                        rs.getTimestamp("created_at").toInstant()
                ),
                filtered
                        ? new Object[]{
                        iban,
                        size,
                        page * size
                }
                        : new Object[]{
                        size,
                        page * size
                }
        );

        return new PageImpl<>(
                content,
                PageRequest.of(page, size),
                total == null ? 0 : total
        );
    }
}
