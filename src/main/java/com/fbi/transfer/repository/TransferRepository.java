package com.fbi.transfer.repository;

import com.fbi.transfer.domain.Transfer;


import java.util.Optional;

public interface TransferRepository {

    Long saveTransfer(
            Transfer transfer);

    Optional<Transfer> findById(Long id);

}