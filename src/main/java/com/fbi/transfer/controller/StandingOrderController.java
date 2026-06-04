package com.fbi.transfer.controller;

import com.fbi.transfer.dto.StandingOrderRequest;
import com.fbi.transfer.dto.StandingOrderResponse;
import com.fbi.transfer.service.StandingOrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/standing-orders")
public class StandingOrderController {

    private final StandingOrderService service;

    public StandingOrderController(StandingOrderService service) {
        this.service = service;
    }

    @PostMapping
    public StandingOrderResponse create(
            @RequestBody StandingOrderRequest request) {

        return service.create(request);
    }

    @GetMapping
    public List<StandingOrderResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public StandingOrderResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @DeleteMapping("/{id}")
    public void cancel(@PathVariable Long id) {
        service.cancel(id);
    }
}