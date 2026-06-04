package com.fbi.transfer.controller;

import com.fbi.transfer.dto.AccountResponse;
import com.fbi.transfer.service.AccountService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public List<AccountResponse> getAccounts() {
        return accountService.findAll();
    }

    @GetMapping("/{iban}")
    public AccountResponse getAccount(@PathVariable String iban) {
        return accountService.findByIban(iban);
    }
}