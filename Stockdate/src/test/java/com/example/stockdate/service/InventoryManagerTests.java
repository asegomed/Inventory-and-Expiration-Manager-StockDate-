package com.example.stockdate.service;

import com.example.stockdate.ExpirationDateType;
import com.example.stockdate.presentation.dto.request.CreateBatchRequest;
import com.example.stockdate.presentation.dto.response.BatchDTO.ExpirationStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InventoryManagerTests {

    private final InventoryManager inventoryManager = new InventoryManager();

    @Test
    void registersAndListsBatchesByExpirationDate() {
        inventoryManager.registerBatch(batch("Arroz", LocalDate.now().plusDays(30)));
        inventoryManager.registerBatch(batch("Leche", LocalDate.now().plusDays(3)));

        assertEquals("Leche", inventoryManager.getBatches().get(0).productName());
        assertEquals(ExpirationStatus.RED, inventoryManager.getBatches().get(0).status());
    }

    @Test
    void rejectsUseByDatesInThePast() {
        CreateBatchRequest request = new CreateBatchRequest(
                "Leche",
                "Lacteos",
                2,
                "Estanteria A",
                LocalDate.now().minusDays(1),
                ExpirationDateType.USE_BY
        );

        assertThrows(IllegalArgumentException.class, () -> inventoryManager.registerBatch(request));
    }

    @Test
    void rejectsNonPositiveQuantities() {
        CreateBatchRequest request = new CreateBatchRequest(
                "Arroz",
                "Cereales",
                0,
                "Estanteria A",
                LocalDate.now().plusDays(30),
                ExpirationDateType.BEST_BEFORE
        );

        assertThrows(IllegalArgumentException.class, () -> inventoryManager.registerBatch(request));
    }

    private CreateBatchRequest batch(String productName, LocalDate expirationDate) {
        return new CreateBatchRequest(
                productName,
                "Cereales",
                4,
                "Estanteria A",
                expirationDate,
                ExpirationDateType.BEST_BEFORE
        );
    }
}
