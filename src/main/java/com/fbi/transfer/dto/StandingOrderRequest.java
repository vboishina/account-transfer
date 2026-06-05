package com.fbi.transfer.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record StandingOrderRequest(
        @NotBlank(message = "Source IBAN is required")
        @Pattern(
                regexp = "^[A-Z]{2}[0-9]{2}[A-Z0-9]{1,30}$",
                message = "Invalid source IBAN format"
        )
        String fromIban,

        @NotBlank(message = "Destination IBAN is required")
        @Pattern(
                regexp = "^[A-Z]{2}[0-9]{2}[A-Z0-9]{1,30}$",
                message = "Invalid destination IBAN format"
        )
        String toIban,

        @NotNull(message = "Amount is required")
        @DecimalMin(
                value = "0.01",
                message = "Amount must be greater than zero"
        )
        BigDecimal amount,

        @NotNull(message = "Execution date is required")
        @FutureOrPresent(
                message = "Execution date must be today or in the future"
        )
        LocalDate executionDate
) {
}