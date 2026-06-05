package com.fbi.transfer.controller;

import com.fbi.transfer.dto.LedgerEntryResponse;
import com.fbi.transfer.service.LedgerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ledger")
public class LedgerController {

    private static final Logger LOG =
            LoggerFactory.getLogger(LedgerController.class);

    private final LedgerService ledgerService;

    public LedgerController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    @GetMapping
    public Page<LedgerEntryResponse> search(
            @RequestParam(required = false) String iban,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        LOG.info(
                "Ledger search request received: iban={}, page={}, size={}",
                iban,
                page,
                size
        );
        Page<LedgerEntryResponse> result =
                ledgerService.search(iban, page, size);

        LOG.info(
                "Ledger search completed: totalElements={}, totalPages={}",
                result.getTotalElements(),
                result.getTotalPages()
        );

        return result;
    }
}