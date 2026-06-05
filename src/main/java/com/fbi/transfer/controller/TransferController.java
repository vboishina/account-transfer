package com.fbi.transfer.controller;

import com.fbi.transfer.dto.TransferRequest;
import com.fbi.transfer.dto.TransferResponse;
import com.fbi.transfer.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transfers")
public class TransferController {
    private static final Logger LOG =
            LoggerFactory.getLogger(TransferController.class);

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping
    @Operation(summary = "Execute a one-time transfer")
    public TransferResponse createTransfer(
            @RequestHeader("X-Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody TransferRequest request) {

        LOG.info(
                "Transfer request received: from={}, to={}, amount={}, idempotencyKey={}",
                request.fromIban(),
                request.toIban(),
                request.amount(),
                idempotencyKey
        );
        TransferResponse response;
        try {

            response = transferService.transfer(idempotencyKey, request);

        } catch (Exception ex) {
            LOG.error(
                    "Transfer request failed: from={}, to={}, amount={}",
                    request.fromIban(),
                    request.toIban(),
                    request.amount(),
                    ex
            );
            throw ex;
        }
        LOG.info(
                "Transfer request completed: transferId={}",
                response.id()
        );
        return response;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get transfer by id")
    public TransferResponse getTransfer(@PathVariable Long id) {

        LOG.info("Get transfer request received: id={}", id);

        TransferResponse response = transferService.getTransfer(id);

        LOG.info("Transfer found: id={}", id);
        return response;
    }
}