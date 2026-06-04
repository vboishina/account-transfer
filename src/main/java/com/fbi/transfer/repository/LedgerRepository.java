package com.fbi.transfer.repository;

import com.fbi.transfer.domain.LedgerEntry;

import java.util.List;

public interface LedgerRepository {

    void saveDebitEntry(
            Long transferId,
            String iban,
            java.math.BigDecimal amount,
            String currency);

    void saveCreditEntry(
            Long transferId,
            String iban,
            java.math.BigDecimal amount,
            String currency);

    List<LedgerEntry> findByAccount(
            String iban,
            int page,
            int size);

}