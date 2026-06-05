package com.fbi.transfer.integration;

import com.fbi.transfer.dto.TransferRequest;
import com.fbi.transfer.dto.TransferResponse;
import com.fbi.transfer.exception.DailyLimitExceededException;
import com.fbi.transfer.exception.InsufficientFundsException;
import com.fbi.transfer.service.TransferService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Tag("integration")
class TransferServiceIntegrationTest {

    @Autowired
    private TransferService transferService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String USD_ACCOUNT = "BG01FINV001";
    private static final String EUR_ACCOUNT = "BG01FINV002";

    @Test
    void shouldTransferSuccessfullyCrossCurrency() {

        BigDecimal amount = new BigDecimal("100");

        BigDecimal sourceBefore = getBalance(USD_ACCOUNT);
        BigDecimal destinationBefore = getBalance(EUR_ACCOUNT);

        TransferResponse response =
                transferService.transfer(
                        "KEY-1",
                        new TransferRequest(
                                USD_ACCOUNT,
                                EUR_ACCOUNT,
                                amount
                        )
                );

        assertNotNull(response);
        assertEquals(USD_ACCOUNT, response.fromIban());
        assertEquals(EUR_ACCOUNT, response.toIban());

        BigDecimal sourceAfter = getBalance(USD_ACCOUNT);
        BigDecimal destinationAfter = getBalance(EUR_ACCOUNT);

        assertEquals(
                sourceBefore.subtract(amount),
                sourceAfter
        );

        BigDecimal creditedAmount =
                amount.multiply(new BigDecimal("0.86"));

        assertEquals(
                destinationBefore.add(creditedAmount),
                destinationAfter
        );
    }

    @Test
    void shouldTransferSuccessfullySameCurrency() {

        String fromIban = "BG01FINV004";
        String toIban = "BG01FINV002";

        BigDecimal amount = new BigDecimal("100");

        BigDecimal sourceBefore = getBalance(fromIban);
        BigDecimal destinationBefore = getBalance(toIban);

        TransferResponse response =
                transferService.transfer(
                        "KEY-2",
                        new TransferRequest(
                                fromIban,
                                toIban,
                                amount
                        )
                );

        assertNotNull(response);

        assertEquals(
                sourceBefore.subtract(amount),
                getBalance(fromIban)
        );

        assertEquals(
                destinationBefore.add(amount),
                getBalance(toIban)
        );
    }

    @Test
    void shouldThrowInsufficientFundsException() {

        assertThrows(
                InsufficientFundsException.class,
                () -> transferService.transfer(
                        "KEY-3",
                        new TransferRequest(
                                USD_ACCOUNT,
                                EUR_ACCOUNT,
                                new BigDecimal("500000")
                        )
                )
        );
    }

    @Test
    void shouldThrowDailyLimitExceededException() {

        jdbcTemplate.update(
                """
                INSERT INTO ACCOUNT_TRANSFER.ledger_entry
                (transfer_id, iban, entry_type, amount, currency, created_at)
                VALUES (?, ?, 'DEBIT', ?, ?, CURRENT_TIMESTAMP)
                """,
                1L,
                USD_ACCOUNT,
                new BigDecimal("19950"),
                "USD"
        );

        assertThrows(
                DailyLimitExceededException.class,
                () -> transferService.transfer(
                        "KEY-4",
                        new TransferRequest(
                                USD_ACCOUNT,
                                EUR_ACCOUNT,
                                new BigDecimal("100")
                        )
                )
        );
    }

    private BigDecimal getBalance(String iban) {

        return jdbcTemplate.queryForObject(
                """
                SELECT balance
                FROM ACCOUNT_TRANSFER.account
                WHERE iban = ?
                """,
                BigDecimal.class,
                iban
        );
    }

    @Test
    void shouldReturnExistingTransferForDuplicateIdempotencyKey() {

        TransferRequest request =
                new TransferRequest(
                        USD_ACCOUNT,
                        EUR_ACCOUNT,
                        new BigDecimal("100")
                );

        TransferResponse first =
                transferService.transfer(
                        "IDEMPOTENT-KEY",
                        request
                );

        TransferResponse second =
                transferService.transfer(
                        "IDEMPOTENT-KEY",
                        request
                );

        assertAll(
                () -> assertEquals(first.id(), second.id()),
                () -> assertEquals(first.fromIban(), second.fromIban()),
                () -> assertEquals(first.toIban(), second.toIban()),
                () -> assertEquals(
                        0,
                        first.amount().compareTo(second.amount())
                ),
                () -> assertEquals(first.currency(), second.currency())
        );
    }
}