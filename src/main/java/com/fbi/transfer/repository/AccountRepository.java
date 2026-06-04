package com.fbi.transfer.repository;

import com.fbi.transfer.domain.Account;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface AccountRepository {

    Optional<Account> findByIban(String iban);

    List<Account> findAll();

    void updateBalance(
            String iban,
            BigDecimal newBalance);

}