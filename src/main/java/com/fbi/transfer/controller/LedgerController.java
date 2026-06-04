package com.fbi.transfer.controller;

import com.fbi.transfer.dto.LedgerEntryResponse;
import com.fbi.transfer.service.LedgerService;
import org.springframework.data.domain.Page;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ledger")
public class LedgerController {

    private final LedgerService ledgerService;


    public LedgerController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    @GetMapping
    public Page<LedgerEntryResponse> search(
            @RequestParam(required = false) String iban,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return (Page<LedgerEntryResponse>) ledgerService.search(iban, page, size);
    }
}