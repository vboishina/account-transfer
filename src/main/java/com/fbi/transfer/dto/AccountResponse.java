package com.fbi.transfer.dto;

import java.math.BigDecimal;

public record AccountResponse(

        String iban,

        String owner,

        String currency,

        BigDecimal balance

) {
}