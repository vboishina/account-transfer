package com.fbi.transfer.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record LedgerEntryResponse(

        Long id,

        Long transferId,

        String accountIban,

        String entryType,

        BigDecimal amount,

        String currency,

        Instant createdAt

) {
}