package com.fbi.transfer.controller;

import com.fbi.transfer.dto.TransferRequest;
import com.fbi.transfer.dto.TransferResponse;
import com.fbi.transfer.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transfers")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping
    @Operation(summary = "Execute a one-time transfer")
    public TransferResponse createTransfer(
            @RequestHeader("X-Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody TransferRequest request) {

        return transferService.transfer(idempotencyKey, request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get transfer by id")
    public TransferResponse getTransfer(@PathVariable Long id) {
        return transferService.getTransfer(id);
    }
}