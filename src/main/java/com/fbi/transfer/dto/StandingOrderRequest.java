package com.fbi.transfer.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record StandingOrderRequest(


        @NotBlank
        String fromIban,

        @NotBlank
        String toIban,

        @NotNull
        @DecimalMin(value = "0.01")
        BigDecimal amount,

        @NotNull
        @FutureOrPresent
        LocalDate executionDate
) {
}