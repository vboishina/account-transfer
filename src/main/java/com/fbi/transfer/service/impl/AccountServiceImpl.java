package com.fbi.transfer.service.impl;

import com.fbi.transfer.dto.AccountResponse;
import com.fbi.transfer.service.AccountService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountServiceImpl implements AccountService {
    @Override
    public List<AccountResponse> findAll() {
        return List.of();
    }

    @Override
    public AccountResponse findByIban(String iban) {
        return null;
    }
}
