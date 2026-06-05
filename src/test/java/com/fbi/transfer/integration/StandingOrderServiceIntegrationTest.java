package com.fbi.transfer.integration;

import com.fbi.transfer.dto.StandingOrderRequest;
import com.fbi.transfer.dto.StandingOrderResponse;
import com.fbi.transfer.dto.enums.StandingOrderStatus;
import com.fbi.transfer.service.StandingOrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class StandingOrderServiceIntegrationTest {

    private static final String FROM_IBAN = "BG01FINV001";
    private static final String TO_IBAN = "BG01FINV002";

    @Autowired
    private StandingOrderService standingOrderService;

    @Test
    void shouldCreateStandingOrder() {

        StandingOrderRequest request =
                new StandingOrderRequest(
                        FROM_IBAN,
                        TO_IBAN,
                        new BigDecimal("250.00"),
                        LocalDate.now().plusDays(5)
                );

        StandingOrderResponse response =
                standingOrderService.create(request);

        assertAll(
                () -> assertNotNull(response.id()),
                () -> assertEquals(FROM_IBAN, response.fromIban()),
                () -> assertEquals(TO_IBAN, response.toIban()),
                () -> assertEquals(
                        0,
                        response.amount().compareTo(new BigDecimal("250.00"))
                ),
                () -> assertEquals(
                        StandingOrderStatus.PENDING,
                        response.status()
                )
        );
    }

    @Test
    void shouldFindStandingOrderById() {

        StandingOrderResponse created =
                standingOrderService.create(
                        new StandingOrderRequest(
                                FROM_IBAN,
                                TO_IBAN,
                                new BigDecimal("100.00"),
                                LocalDate.now().plusDays(1)
                        )
                );

        StandingOrderResponse found =
                standingOrderService.findById(created.id());

        assertAll(
                () -> assertEquals(created.id(), found.id()),
                () -> assertEquals(created.fromIban(), found.fromIban()),
                () -> assertEquals(created.toIban(), found.toIban()),
                () -> assertEquals(
                        0,
                        created.amount().compareTo(found.amount())
                )
        );
    }

    @Test
    void shouldReturnAllStandingOrders() {

        standingOrderService.create(
                new StandingOrderRequest(
                        FROM_IBAN,
                        TO_IBAN,
                        new BigDecimal("100.00"),
                        LocalDate.now().plusDays(1)
                )
        );

        standingOrderService.create(
                new StandingOrderRequest(
                        FROM_IBAN,
                        TO_IBAN,
                        new BigDecimal("200.00"),
                        LocalDate.now().plusDays(2)
                )
        );

        List<StandingOrderResponse> orders =
                standingOrderService.findAll();

        assertFalse(orders.isEmpty());
        assertTrue(orders.size() >= 2);
    }

    @Test
    void shouldCancelStandingOrder() {

        StandingOrderResponse created =
                standingOrderService.create(
                        new StandingOrderRequest(
                                FROM_IBAN,
                                TO_IBAN,
                                new BigDecimal("150.00"),
                                LocalDate.now().plusDays(3)
                        )
                );

        standingOrderService.cancel(created.id());

        StandingOrderResponse cancelled =
                standingOrderService.findById(created.id());

        assertEquals(
                StandingOrderStatus.CANCELLED,
                cancelled.status()
        );
    }

    @Test
    void shouldNotCancelAlreadyCancelledStandingOrder() {

        StandingOrderResponse created =
                standingOrderService.create(
                        new StandingOrderRequest(
                                FROM_IBAN,
                                TO_IBAN,
                                new BigDecimal("150.00"),
                                LocalDate.now().plusDays(3)
                        )
                );

        standingOrderService.cancel(created.id());
        standingOrderService.cancel(created.id());

        StandingOrderResponse order =
                standingOrderService.findById(created.id());

        assertEquals(
                StandingOrderStatus.CANCELLED,
                order.status()
        );
    }
}