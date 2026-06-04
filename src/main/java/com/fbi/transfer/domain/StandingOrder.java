package com.fbi.transfer.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public record StandingOrder(

        Long id,

        String sourceIban,

        String destinationIban,

        BigDecimal amount,

        LocalDate firstExecutionDate,

        LocalDate nextExecutionDate,

        Integer executionDayOfMonth,

        boolean active

) {
}