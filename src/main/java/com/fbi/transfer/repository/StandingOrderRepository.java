package com.fbi.transfer.repository;

import com.fbi.transfer.domain.StandingOrder;


import java.util.List;
import java.util.Optional;

public interface StandingOrderRepository {

    Long save(
            StandingOrder order);

    List<StandingOrder> findAll();

    Optional<StandingOrder> findById(Long id);

    void deactivate(Long id);

}