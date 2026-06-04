package com.fbi.transfer.service.impl;

import com.fbi.transfer.dto.TransferRequest;
import com.fbi.transfer.dto.TransferResponse;
import com.fbi.transfer.exception.AccountNotFoundException;
import com.fbi.transfer.exception.DailyLimitExceededException;
import com.fbi.transfer.exception.InsufficientFundsException;
import com.fbi.transfer.service.TransferService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;

@Service
public class TransferServiceImpl implements TransferService {

    private final JdbcTemplate jdbcTemplate;

    @Value("${fx.rate.usd-to-eur:0.86}")
    private BigDecimal usdToEurRate;

    public TransferServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public TransferResponse transfer(String idempotencyKey, TransferRequest request) {

        Instant now = Instant.now();
        BigDecimal amount = request.amount();

        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }

        // -----------------------------------
        // 1. LOAD ACCOUNTS
        // -----------------------------------
        Map<String, Object> source = findAccount(request.fromIban());
        Map<String, Object> destination = findAccount(request.toIban());

        if (source == null || destination == null) {
            throw new AccountNotFoundException();
        }

        String sourceCurrency = (String) source.get("CURRENCY");
        String destinationCurrency = (String) destination.get("CURRENCY");

        BigDecimal sourceBalance = (BigDecimal) source.get("BALANCE");
        // -----------------------------------
        // 3. BALANCE CHECK (BUSINESS RULE)
        // -----------------------------------
        if (sourceBalance.compareTo(amount) < 0) {
            throw new InsufficientFundsException();
        }
        // -----------------------------------
        // 2. DAILY LIMIT CHECK
        // -----------------------------------
        BigDecimal todayOutgoing = jdbcTemplate.queryForObject(
                """
                        SELECT COALESCE(SUM(amount), 0)
                        FROM ACCOUNT_TRANSFER.ledger_entry
                        WHERE iban = ?
                          AND entry_type = 'DEBIT'
                          AND CAST(created_at AS DATE) = ?
                        """,
                BigDecimal.class,
                request.fromIban(),
                LocalDate.now()
        );

        if (todayOutgoing == null) {
            todayOutgoing = BigDecimal.ZERO;
        }

        if (todayOutgoing.add(amount).compareTo(BigDecimal.valueOf(20000)) > 0) {
            throw new DailyLimitExceededException();
        }

        // -----------------------------------
        // 3. BALANCE CHECK (ATOMIC UPDATE)
        // -----------------------------------
        int updated = jdbcTemplate.update(
                """
                        UPDATE ACCOUNT_TRANSFER.account
                        SET balance = balance - ?
                        WHERE iban = ? AND balance >= ?
                        """,
                amount,
                request.fromIban(),
                amount
        );

        if (updated == 0) {
            throw new IllegalStateException("Insufficient funds");
        }

        // -----------------------------------
        // 4. FX CONVERSION
        // -----------------------------------
        BigDecimal fxRate = usdToEurRate;

        BigDecimal creditedAmount;
        if (sourceCurrency.equals(destinationCurrency)) {
            creditedAmount = amount;
        } else if ("USD".equals(sourceCurrency) && "EUR".equals(destinationCurrency)) {
            creditedAmount = amount.multiply(fxRate);
        } else if ("EUR".equals(sourceCurrency) && "USD".equals(destinationCurrency)) {
            creditedAmount = amount.divide(fxRate, 2, RoundingMode.HALF_UP);
        } else {
            throw new IllegalArgumentException("Unsupported currency pair");
        }

        jdbcTemplate.update(
                """
                        UPDATE ACCOUNT_TRANSFER.account
                        SET balance = balance + ?
                        WHERE iban = ?
                        """,
                creditedAmount,
                request.toIban()
        );

        // -----------------------------------
        // 5. INSERT TRANSFER (IDEMPOTENT)
        // -----------------------------------
        Long transferId;

        try {
            transferId = insertTransfer(
                    idempotencyKey,
                    request,
                    sourceCurrency,
                    fxRate,
                    creditedAmount,
                    now
            );
        } catch (DuplicateKeyException ex) {
            return getTransferByIdempotencyKey(idempotencyKey);
        }
         // -----------------------------------
        // 6. LEDGER (DOUBLE ENTRY)
        // -----------------------------------
        jdbcTemplate.update(
                """
                INSERT INTO ACCOUNT_TRANSFER.ledger_entry
                (transfer_id, iban, entry_type, amount, currency, created_at)
                VALUES (?, ?, 'DEBIT', ?, ?, ?)
                """,
                transferId,
                request.fromIban(),
                amount,
                sourceCurrency,
                Timestamp.from(now)
        );

        jdbcTemplate.update(
                """
                INSERT INTO ACCOUNT_TRANSFER.ledger_entry
                (transfer_id, iban, entry_type, amount, currency, created_at)
                VALUES (?, ?, 'CREDIT', ?, ?, ?)
                """,
                transferId,
                request.toIban(),
                creditedAmount,
                destinationCurrency,
                Timestamp.from(now)
        );

        // -----------------------------------
        // 7. RESPONSE
        // -----------------------------------
        return new TransferResponse(
                transferId,
                request.fromIban(),
                request.toIban(),
                amount,
                sourceCurrency,
                fxRate,
                now
        );
    }

    // -----------------------------------
    // INSERT TRANSFER
    // -----------------------------------
    private Long insertTransfer(
            String idempotencyKey,
            TransferRequest request,
            String currency,
            BigDecimal fxRate,
            BigDecimal creditedAmount,
            Instant now
    ) {

        var keyHolder = new org.springframework.jdbc.support.GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            var ps = connection.prepareStatement(
                    """
                            INSERT INTO ACCOUNT_TRANSFER.transfer
                            (idempotency_key, from_iban, to_iban, amount, currency, fx_rate, created_at)
                            VALUES (?, ?, ?, ?, ?, ?, ?)
                            """,
                    new String[]{"id"}
            );

            ps.setString(1, idempotencyKey);
            ps.setString(2, request.fromIban());
            ps.setString(3, request.toIban());
            ps.setBigDecimal(4, request.amount());
            ps.setString(5, currency);
            ps.setBigDecimal(6, fxRate);
            ps.setTimestamp(7, Timestamp.from(now));

            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    // -----------------------------------
    // IDEMPOTENCY RETRY FETCH
    // -----------------------------------
    private TransferResponse getTransferByIdempotencyKey(String key) {

        return jdbcTemplate.queryForObject(
                """
                        SELECT id, from_iban, to_iban, amount, currency, fx_rate, created_at
                        FROM ACCOUNT_TRANSFER.transfer
                        WHERE idempotency_key = ?
                        """,
                (rs, rowNum) -> new TransferResponse(
                        rs.getLong("id"),
                        rs.getString("from_iban"),
                        rs.getString("to_iban"),
                        rs.getBigDecimal("amount"),
                        rs.getString("currency"),
                        rs.getBigDecimal("fx_rate"),
                        rs.getTimestamp("created_at").toInstant()
                ),
                key
        );
    }

    private Map<String, Object> findAccount(String iban) {
        try {
            return jdbcTemplate.queryForMap(
                    "SELECT iban, balance, currency FROM ACCOUNT_TRANSFER.account WHERE iban = ?",
                    iban
            );
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public TransferResponse getTransfer(Long id) {
        return jdbcTemplate.queryForObject(
                """
                        SELECT id, from_iban, to_iban, amount, currency, fx_rate, created_at
                        FROM ACCOUNT_TRANSFER.transfer
                        WHERE id = ?
                        """,
                (rs, rowNum) -> new TransferResponse(
                        rs.getLong("id"),
                        rs.getString("from_iban"),
                        rs.getString("to_iban"),
                        rs.getBigDecimal("amount"), // creditedAmount not stored in table
                        rs.getString("currency"),
                        rs.getBigDecimal("fx_rate"),
                        rs.getTimestamp("created_at").toInstant()
                ),
                id
        );
    }
}