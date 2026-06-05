package com.fbi.transfer.controller;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "fbi.auth.api-key=test123"
        }
)
class TransferControllerIntegrationTest {

    @LocalServerPort
    private int port;


    @Test
    void shouldCreateTransfer() throws Exception {

        String fromIban = "BG01FINV001";
        String toIban = "BG01FINV002";
        BigDecimal amount = new BigDecimal("0.01");

        String json = """
            {
              "fromIban":"BG01FINV001",
              "toIban":"BG01FINV002",
              "amount":0.01
            }
            """;

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(new URI(
                                "http://localhost:" + port + "/api/v1/transfers"
                        ))
                        .header("Content-Type", "application/json")
                        .header("X-FIB-AUTH", "test123")
                        .header(
                                "X-Idempotency-Key",
                                UUID.randomUUID().toString()
                        )
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();

        HttpResponse<String> response =
                HttpClient.newHttpClient()
                        .send(
                                request,
                                HttpResponse.BodyHandlers.ofString()
                        );

        assertEquals(200, response.statusCode());


        String body = response.body();

        assertAll(
                () -> assertTrue(body.contains("\"fromIban\":\"BG01FINV001\"")),
                () -> assertTrue(body.contains("\"toIban\":\"BG01FINV002\"")),
                () -> assertTrue(body.contains("\"amount\":0.01")));
    }

    @Test
    void shouldGetTransferById() throws Exception {

        String createJson = """
        {
          "fromIban":"BG01FINV001",
          "toIban":"BG01FINV002",
          "amount":0.01
        }
        """;

        HttpResponse<String> createResponse =
                HttpClient.newHttpClient().send(
                        HttpRequest.newBuilder()
                                .uri(new URI(
                                        "http://localhost:" + port + "/api/v1/transfers"
                                ))
                                .header("Content-Type", "application/json")
                                .header("X-FIB-AUTH", "test123")
                                .header(
                                        "X-Idempotency-Key",
                                        UUID.randomUUID().toString()
                                )
                                .POST(HttpRequest.BodyPublishers.ofString(createJson))
                                .build(),
                        HttpResponse.BodyHandlers.ofString()
                );

        assertEquals(200, createResponse.statusCode());

        String createBody = createResponse.body();

        assertAll(
                () -> assertTrue(createBody.contains("\"fromIban\":\"BG01FINV001\"")),
                () -> assertTrue(createBody.contains("\"toIban\":\"BG01FINV002\"")),
                () -> assertTrue(createBody.contains("\"amount\":0.01"))
        );

        // extract generated id from create response
        Long id = Long.valueOf(
                createBody.replaceAll(".*\"id\":(\\d+).*", "$1")
        );

        HttpResponse<String> getResponse =
                HttpClient.newHttpClient().send(
                        HttpRequest.newBuilder()
                                .uri(new URI(
                                        "http://localhost:" + port + "/api/v1/transfers/" + id
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
                () -> assertTrue(getBody.contains("\"id\":" + id)),
                () -> assertTrue(getBody.contains("\"fromIban\":\"BG01FINV001\"")),
                () -> assertTrue(getBody.contains("\"toIban\":\"BG01FINV002\"")),
                () -> assertTrue(getBody.contains("\"amount\":0.01"))
        );
    }

    @Test
    void shouldReturnLedgerEntriesForAccount() throws Exception {

        String transferJson = """
        {
          "fromIban":"BG01FINV001",
          "toIban":"BG01FINV002",
          "amount":0.01
        }
        """;

        HttpClient client = HttpClient.newHttpClient();

        // Create transfer #1
        HttpResponse<String> createResponse1 =
                client.send(
                        HttpRequest.newBuilder()
                                .uri(new URI(
                                        "http://localhost:" + port + "/api/v1/transfers"
                                ))
                                .header("Content-Type", "application/json")
                                .header("X-FIB-AUTH", "test123")
                                .header(
                                        "X-Idempotency-Key",
                                        UUID.randomUUID().toString()
                                )
                                .POST(HttpRequest.BodyPublishers.ofString(transferJson))
                                .build(),
                        HttpResponse.BodyHandlers.ofString()
                );

        assertEquals(200, createResponse1.statusCode());

        // Create transfer #2
        HttpResponse<String> createResponse2 =
                client.send(
                        HttpRequest.newBuilder()
                                .uri(new URI(
                                        "http://localhost:" + port + "/api/v1/transfers"
                                ))
                                .header("Content-Type", "application/json")
                                .header("X-FIB-AUTH", "test123")
                                .header(
                                        "X-Idempotency-Key",
                                        UUID.randomUUID().toString()
                                )
                                .POST(HttpRequest.BodyPublishers.ofString(transferJson))
                                .build(),
                        HttpResponse.BodyHandlers.ofString()
                );

        assertEquals(200, createResponse2.statusCode());

        // Query ledger
        HttpResponse<String> ledgerResponse =
                client.send(
                        HttpRequest.newBuilder()
                                .uri(new URI(
                                        "http://localhost:" + port
                                                + "/api/v1/ledger?iban=BG01FINV001&page=0&size=20"
                                ))
                                .header("accept", "*/*")
                                .header("X-FIB-AUTH", "test123")
                                .GET()
                                .build(),
                        HttpResponse.BodyHandlers.ofString()
                );

        assertEquals(200, ledgerResponse.statusCode());

        String body = ledgerResponse.body();
        System.out.println(body);
        assertAll(
                () -> assertTrue(
                        Integer.parseInt(
                                body.replaceAll(
                                        ".*\"numberOfElements\":(\\d+).*",
                                        "$1"
                                )
                        ) >= 2
                ),
                () -> assertTrue(body.contains("\"accountIban\":\"BG01FINV001\"")),
                () -> assertTrue(body.contains("\"entryType\":\"DEBIT\"")),

                // verify both transfers generated ledger rows
                () -> assertTrue(
                        body.split("\"entryType\":\"DEBIT\"").length - 1 >= 2
                ),

                () -> assertTrue(
                        body.split("\"accountIban\":\"BG01FINV001\"").length - 1 >= 2
                )
        );
    }
}