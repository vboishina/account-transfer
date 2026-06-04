package com.fbi.transfer.dto;

import com.fbi.transfer.dto.enums.StandingOrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record StandingOrderResponse(

        Long id,

        String fromIban,

        String toIban,

        BigDecimal amount,

        LocalDate executionDate,

        StandingOrderStatus status,

        Instant createdAt

) {
}