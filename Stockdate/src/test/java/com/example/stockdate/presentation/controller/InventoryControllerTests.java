package com.example.stockdate.presentation.controller;

import com.example.stockdate.ExpirationDateType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "stockdate.browser.open=false"
)
class InventoryControllerTests {

    @LocalServerPort
    private int port;

    @Test
    void registersAndReturnsInventoryBatches() throws IOException, InterruptedException {
        String expirationDate = LocalDate.now().plusDays(30).toString();
        String requestBody = """
                {
                  "productName": "Arroz",
                  "category": "Cereales",
                  "quantity": 10,
                  "storageLocation": "Estanteria A",
                  "expirationDate": "%s",
                  "expirationDateType": "BEST_BEFORE"
                }
                """.formatted(expirationDate);

        HttpResponse<String> created = send(requestBody);

        assertEquals(201, created.statusCode());
        assertTrue(created.body().contains("\"productName\":\"Arroz\""));

        HttpResponse<String> inventory = send(null);

        assertEquals(200, inventory.statusCode());
        assertTrue(inventory.body().contains("\"productName\":\"Arroz\""));
    }

    @Test
    void returnsBadRequestForInvalidBatch() throws IOException, InterruptedException {
        String requestBody = """
                {
                  "productName": "Arroz",
                  "category": "Cereales",
                  "quantity": 0,
                  "storageLocation": "Estanteria A",
                  "expirationDate": "%s",
                  "expirationDateType": "%s"
                }
                """.formatted(
                LocalDate.now().plusDays(30),
                ExpirationDateType.BEST_BEFORE
        );

        HttpResponse<String> response = send(requestBody);

        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("La cantidad debe ser mayor que cero."));
    }

    private HttpResponse<String> send(String body) throws IOException, InterruptedException {
        HttpRequest.Builder request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/inventory"));
        if (body == null) {
            request.GET();
        } else {
            request.header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body));
        }
        return HttpClient.newHttpClient().send(request.build(), HttpResponse.BodyHandlers.ofString());
    }
}
