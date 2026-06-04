package com.fbi.transfer.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record TransferResponse(
        Long id,
        String fromIban,
        String toIban,
        BigDecimal amount,
        String currency,
        BigDecimal fxRate,
        Instant createdAt
) {}