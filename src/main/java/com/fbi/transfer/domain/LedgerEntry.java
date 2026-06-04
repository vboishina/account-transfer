package com.fbi.transfer.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record LedgerEntry(

        Long id,

        Long transferId,

        String accountIban,

        LedgerEntryType entryType,

        Currency currency,

        BigDecimal amount,

        Instant createdAt

) {
}
