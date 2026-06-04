package com.fbi.transfer.service;

import com.fbi.transfer.dto.LedgerEntryResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface LedgerService {

    Page<LedgerEntryResponse> search(
            String iban,
            int page,
            int size);

}