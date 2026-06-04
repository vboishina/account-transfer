package com.fbi.transfer.service;

import com.fbi.transfer.dto.StandingOrderRequest;
import com.fbi.transfer.dto.StandingOrderResponse;

import java.util.List;

public interface StandingOrderService {

    StandingOrderResponse create(
            StandingOrderRequest request);

    List<StandingOrderResponse> findAll();

    StandingOrderResponse findById(Long id);

    void cancel(Long id);

}