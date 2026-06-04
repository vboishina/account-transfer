package com.fbi.transfer.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public record TransferRequest(

        @NotBlank
        String fromIban,

        @NotBlank
        String toIban,

        @DecimalMin("0.01")
        BigDecimal amount

) {}