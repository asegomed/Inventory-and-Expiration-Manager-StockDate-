package stockdate.service;

import stockdate.ExpirationDateType;
import stockdate.presentation.dto.request.CreateBatchRequest;
import stockdate.presentation.dto.response.BatchDTO;
import stockdate.presentation.dto.response.BatchDTO.ExpirationStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import stockdate.persistence.repository.InventoryRepository;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:sqlite::memory:",
        "spring.datasource.hikari.maximum-pool-size=1",
        "stockdate.browser.open=false"
})
class InventoryManagerTests {

    @Autowired
    private InventoryManager inventoryManager;

    @Autowired
    private InventoryRepository inventoryRepository;

    @BeforeEach
    void clearBatches() {
        inventoryRepository.deleteAll();
    }

    @Test
    void registersAndListsBatchesByExpirationDate() {
        inventoryManager.registerBatch(batch("Rice", LocalDate.now().plusDays(30)));
        inventoryManager.registerBatch(batch("Milk", LocalDate.now().plusDays(3)));

        assertEquals("Milk", inventoryManager.getBatches().get(0).productName());
        assertEquals(ExpirationStatus.RED, inventoryManager.getBatches().get(0).status());
    }

    @Test
    void rejectsUseByDatesInThePast() {
        CreateBatchRequest request = new CreateBatchRequest(
                "Milk",
                "Dairy",
                2,
                "Shelf A",
                LocalDate.now().minusDays(1),
                ExpirationDateType.USE_BY
        );

        assertThrows(IllegalArgumentException.class, () -> inventoryManager.registerBatch(request));
    }

    @Test
    void rejectsNonPositiveQuantities() {
        CreateBatchRequest request = new CreateBatchRequest(
                "Rice",
                "Grains",
                0,
                "Shelf A",
                LocalDate.now().plusDays(30),
                ExpirationDateType.BEST_BEFORE
        );

        assertThrows(IllegalArgumentException.class, () -> inventoryManager.registerBatch(request));
    }

    @Test
    void updatesBatchAndPersistsChanges() {
        BatchDTO created = inventoryManager.registerBatch(batch("Rice", LocalDate.now().plusDays(30)));
        CreateBatchRequest changes = new CreateBatchRequest(
                "Brown rice",
                "Grains",
                8,
                "Shelf B",
                LocalDate.now().plusDays(45),
                ExpirationDateType.BEST_BEFORE
        );

        BatchDTO updated = inventoryManager.updateBatch(created.id(), changes);

        assertEquals(created.id(), updated.id());
        assertEquals("Brown rice", updated.productName());
        assertEquals("Brown rice", inventoryManager.getBatches().get(0).productName());
        assertEquals(8, inventoryManager.getBatches().get(0).quantity());
    }

    private CreateBatchRequest batch(String productName, LocalDate expirationDate) {
        return new CreateBatchRequest(
                productName,
                "Grains",
                4,
                "Shelf A",
                expirationDate,
                ExpirationDateType.BEST_BEFORE
        );
    }
}
