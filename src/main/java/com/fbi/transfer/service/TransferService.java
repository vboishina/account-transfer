package com.fbi.transfer.service;

import com.fbi.transfer.dto.TransferRequest;
import com.fbi.transfer.dto.TransferResponse;


public interface TransferService {


    TransferResponse transfer(String idempotencyKey, TransferRequest request);

    TransferResponse getTransfer(Long id);
}