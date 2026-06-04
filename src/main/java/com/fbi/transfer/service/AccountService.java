package com.fbi.transfer.service;

import com.fbi.transfer.dto.AccountResponse;

import java.util.List;

public interface AccountService {

    List<AccountResponse> findAll();

    AccountResponse findByIban(String iban);

}