package com.fbi.transfer.domain;

import java.math.BigDecimal;

public record Account(

        String iban,

        String owner,

        Currency currency,

        BigDecimal balance

) {
}