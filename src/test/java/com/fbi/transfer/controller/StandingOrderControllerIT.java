package com.fbi.transfer.controller;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;


import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "fbi.auth.api-key=test123",
                "standing-order.scheduler.cron=*/5 * * * * *"
        }
)
public class StandingOrderControllerIT {

    @LocalServerPort
    private int port;

    @Test
    void shouldExecuteStandingOrderViaScheduler() throws Exception {

        String json = """
        {
          "fromIban":"BG01FINV001",
          "toIban":"BG01FINV002",
          "amount":0.01,
          "executionDate":"%s"
        }
        """.formatted(LocalDate.now());

        HttpClient client = HttpClient.newHttpClient();

        HttpResponse<String> createResponse =
                client.send(
                        HttpRequest.newBuilder()
                                .uri(new URI(
                                        "http://localhost:" + port + "/api/v1/standing-orders"
                                ))
                                .header("Content-Type", "application/json")
                                .header("X-FIB-AUTH", "test123")
                                .POST(HttpRequest.BodyPublishers.ofString(json))
                                .build(),
                        HttpResponse.BodyHandlers.ofString()
                );

        assertEquals(200, createResponse.statusCode());

        String createBody = createResponse.body();

        assertAll(
                () -> assertTrue(
                        createBody.contains("\"fromIban\":\"BG01FINV001\"")
                ),
                () -> assertTrue(
                        createBody.contains("\"toIban\":\"BG01FINV002\"")
                ),
                () -> assertTrue(
                        createBody.contains("\"amount\":0.01")
                ),
                () -> assertTrue(
                        createBody.contains("\"status\":\"PENDING\"")
                )
        );

        Long standingOrderId = Long.valueOf(
                createBody.replaceAll(".*\"id\":(\\d+).*", "$1")
        );

        // scheduler runs every minute
        Thread.sleep(10_000);

        HttpResponse<String> getResponse =
                client.send(
                        HttpRequest.newBuilder()
                                .uri(new URI(
                                        "http://localhost:"
                                                + port
                                                + "/api/v1/standing-orders/"
                                                + standingOrderId
                                ))
                                .header("accept", "*/*")
                                .header("X-FIB-AUTH", "test123")
                                .GET()
                                .build(),
                        HttpResponse.BodyHandlers.ofString()
                );

        assertEquals(200, getResponse.statusCode());

        String getBody = getResponse.body();

        assertAll(
                () -> assertTrue(
                        getBody.contains("\"id\":" + standingOrderId)
                ),
                () -> assertTrue(
                        getBody.contains("\"fromIban\":\"BG01FINV001\"")
                ),
                () -> assertTrue(
                        getBody.contains("\"toIban\":\"BG01FINV002\"")
                ),
                () -> assertTrue(
                        getBody.contains("\"amount\":0.01")
                ),
                () -> assertTrue(
                        getBody.contains("\"status\":\"COMPLETED\"")
                )
        );
    }
}
