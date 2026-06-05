package com.fbi.transfer.controller;

import com.fbi.transfer.dto.AccountResponse;
import com.fbi.transfer.service.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private static final Logger LOG =
            LoggerFactory.getLogger(AccountController.class);

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public List<AccountResponse> getAccounts() {
        LOG.info("Get all accounts request received");

        List<AccountResponse> accounts = accountService.findAll();

        LOG.info("Get all accounts completed: count={}", accounts.size());

        return accounts;
    }

    @GetMapping("/{iban}")
    public AccountResponse getAccount(@PathVariable String iban) {
        LOG.info("Get account request received: iban={}", iban);

        AccountResponse account = accountService.findByIban(iban);

        LOG.info("Get account completed: iban={}", iban);
        return account;
    }
}