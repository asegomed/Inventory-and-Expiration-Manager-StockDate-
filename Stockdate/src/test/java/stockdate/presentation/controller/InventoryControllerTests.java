package stockdate.presentation.controller;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;
import stockdate.ExpirationDateType;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "stockdate.browser.open=false",
                "spring.datasource.url=jdbc:sqlite::memory:",
                "spring.datasource.hikari.maximum-pool-size=1"
        }
)
@Sql(statements = "INSERT OR IGNORE INTO category (id, name) VALUES (1, 'Grains')")
class InventoryControllerTests {

    @LocalServerPort
    private int port;

    @Test
    void registersAndReturnsInventoryBatches() throws IOException, InterruptedException {
        String expirationDate = LocalDate.now().plusDays(30).toString();
        String requestBody = """
                {
                  "productName": "Rice",
                  "categoryId": 1,
                  "quantity": 10,
                  "storageLocation": "Shelf A",
                  "expirationDate": "%s",
                  "expirationDateType": "BEST_BEFORE"
                }
                """.formatted(expirationDate);

        HttpResponse<String> created = send(requestBody);

        assertEquals(201, created.statusCode());
        assertTrue(created.body().contains("\"productName\":\"Rice\""));

        HttpResponse<String> inventory = send(null);

        assertEquals(200, inventory.statusCode());
        assertTrue(inventory.body().contains("\"productName\":\"Rice\""));
    }

    @Test
    void returnsBadRequestForInvalidBatch() throws IOException, InterruptedException {
        String requestBody = """
                {
                  "productName": "Rice",
                  "categoryId": 1,
                  "quantity": 0,
                  "storageLocation": "Shelf A",
                  "expirationDate": "%s",
                  "expirationDateType": "%s"
                }
                """.formatted(
                LocalDate.now().plusDays(30),
                ExpirationDateType.BEST_BEFORE
        );

        HttpResponse<String> response = send(requestBody);

        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("Quantity must be greater than zero."));
    }

    @Test
    void updatesAndReturnsExistingBatch() throws IOException, InterruptedException {
        String createBody = batchJson("Rice", 1L, 10, "Shelf A",
                LocalDate.now().plusDays(30), "BEST_BEFORE");
        HttpResponse<String> created = send(createBody);
        assertEquals(201, created.statusCode());
        long id = Long.parseLong(created.body().replaceFirst(".*\"id\":(\\d+).*", "$1"));

        String updateBody = batchJson("Brown rice", 1L, 15, "Shelf B",
                LocalDate.now().plusDays(45), "BEST_BEFORE");
        HttpResponse<String> updated = send("PUT", "/api/inventory/" + id, updateBody);

        assertEquals(200, updated.statusCode());
        assertTrue(updated.body().contains("\"productName\":\"Brown rice\""));
        assertTrue(updated.body().contains("\"quantity\":15"));
        assertTrue(send(null).body().contains("\"productName\":\"Brown rice\""));
    }

    @Test
    void returnsNotFoundWhenUpdatingMissingBatch() throws IOException, InterruptedException {
        String updateBody = batchJson("Rice", 1L, 5, "Shelf A",
                LocalDate.now().plusDays(30), "BEST_BEFORE");

        HttpResponse<String> response = send("PUT", "/api/inventory/999999", updateBody);

        assertEquals(404, response.statusCode());
        assertTrue(response.body().contains("Batch not found."));
    }

    @Test
    void deletesExistingBatch() throws IOException, InterruptedException {
        HttpResponse<String> created = send(batchJson("Beans", 1L, 6, "Shelf C",
                LocalDate.now().plusDays(60), "BEST_BEFORE"));
        assertEquals(201, created.statusCode());
        long id = Long.parseLong(created.body().replaceFirst(".*\"id\":(\\d+).*", "$1"));

        HttpResponse<String> deleted = send("DELETE", "/api/inventory/" + id, null);

        assertEquals(204, deleted.statusCode());
        assertFalse(send(null).body().contains("\"id\":" + id + ","));
    }

    @Test
    void returnsNotFoundWhenDeletingMissingBatch() throws IOException, InterruptedException {
        HttpResponse<String> response = send("DELETE", "/api/inventory/999999", null);

        assertEquals(404, response.statusCode());
        assertTrue(response.body().contains("Batch not found."));
    }

    @Test
    void returnsDashboardSummary() throws IOException, InterruptedException {
        send(batchJson("Milk", 1L, 2, "Fridge", LocalDate.now().plusDays(2), "USE_BY"));

        HttpResponse<String> response = send("GET", "/api/dashboard", null);

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("\"redCount\""));
        assertTrue(response.body().contains("\"pinnedBatches\""));
    }

    private HttpResponse<String> send(String body) throws IOException, InterruptedException {
        return send(body == null ? "GET" : "POST", "/api/inventory", body);
    }

    private HttpResponse<String> send(String method, String path, String body)
            throws IOException, InterruptedException {
        HttpRequest.Builder request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path));
        if ("DELETE".equals(method)) {
            request.DELETE();
        } else if (body == null) {
            request.GET();
        } else {
            request.header("Content-Type", "application/json");
            if ("PUT".equals(method)) {
                request.PUT(HttpRequest.BodyPublishers.ofString(body));
            } else {
                request.POST(HttpRequest.BodyPublishers.ofString(body));
            }
        }
        return HttpClient.newHttpClient().send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    private String batchJson(String productName, long categoryId, int quantity,
                             String location, LocalDate expirationDate, String dateType) {
        return """
                {
                  "productName": "%s",
                  "categoryId": %d,
                  "quantity": %d,
                  "storageLocation": "%s",
                  "expirationDate": "%s",
                  "expirationDateType": "%s"
                }
                """.formatted(productName, categoryId, quantity, location, expirationDate, dateType);
    }
}
