package com.fbi.transfer.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record Transfer(

        Long id,

        String idempotencyKey,

        String sourceIban,

        String destinationIban,

        Currency sourceCurrency,

        Currency destinationCurrency,

        BigDecimal amount,

        BigDecimal exchangedAmount,

        TransferStatus status,

        Instant createdAt

) {
}