package com.fbi.transfer.domain;


import java.time.Instant;

public record IdempotencyKey(

        String key,

        Long transferId,

        Instant expiresAt

) {
}