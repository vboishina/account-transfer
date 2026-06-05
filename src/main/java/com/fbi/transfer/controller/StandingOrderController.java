package com.fbi.transfer.controller;

import com.fbi.transfer.dto.StandingOrderRequest;
import com.fbi.transfer.dto.StandingOrderResponse;
import com.fbi.transfer.service.StandingOrderService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/standing-orders")
public class StandingOrderController {

    private static final Logger LOG =
            LoggerFactory.getLogger(StandingOrderController.class);

    private final StandingOrderService service;

    public StandingOrderController(StandingOrderService service) {
        this.service = service;
    }

    @PostMapping
    public StandingOrderResponse create(
          @Valid @RequestBody StandingOrderRequest request) {

        LOG.info(
                "Create standing order request received: from={}, to={}, amount={}",
                request.fromIban(),
                request.toIban(),
                request.amount()
        );

        StandingOrderResponse response = service.create(request);

        LOG.info(
                "Standing order created successfully: id={}",
                response.id()
        );

        return response;
    }

    @GetMapping
    public List<StandingOrderResponse> findAll() {

        LOG.info("Get all standing orders request received");

        List<StandingOrderResponse> orders = service.findAll();

        LOG.info(
                "Get all standing orders completed: count={}",
                orders.size()
        );

        return orders;
    }

    @GetMapping("/{id}")
    public StandingOrderResponse findById(@PathVariable Long id) {

        LOG.info(
                "Get standing order request received: id={}",
                id
        );

        StandingOrderResponse response = service.findById(id);

        LOG.info(
                "Get standing order completed: id={}",
                id
        );

        return response;
    }

    @DeleteMapping("/{id}")
    public void cancel(@PathVariable Long id) {

        LOG.info(
                "Cancel standing order request received: id={}",
                id
        );

        service.cancel(id);

        LOG.info(
                "Standing order cancelled successfully: id={}",
                id
        );
    }
}