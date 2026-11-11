package stockdate.service.factory;

import org.junit.jupiter.api.Test;
import stockdate.ExpirationDateType;
import stockdate.persistence.entity.BatchEntity;
import stockdate.presentation.dto.request.CreateBatchRequest;
import stockdate.presentation.dto.response.BatchDTO;
import stockdate.presentation.dto.response.BatchDTO.ExpirationStatus;
import stockdate.service.strategy.BestBeforeStrategy;
import stockdate.service.strategy.UseByStrategy;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FactoryTests {

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 11);

    private final Factory factory = new Factory(List.of(new UseByStrategy(), new BestBeforeStrategy()), 7, 14);

    @Test
    void createsTheStrategyForEachDateType() {
        assertInstanceOf(UseByStrategy.class, factory.createStrategy(ExpirationDateType.USE_BY));
        assertInstanceOf(BestBeforeStrategy.class, factory.createStrategy(ExpirationDateType.BEST_BEFORE));
    }

    @Test
    void failsWhenADateTypeHasNoStrategy() {
        List<UseByStrategy> onlyUseBy = List.of(new UseByStrategy());
        assertThrows(IllegalStateException.class, () -> new Factory(List.copyOf(onlyUseBy), 7, 14));
    }

    @Test
    void failsWithInvalidThresholds() {
        assertThrows(IllegalStateException.class,
                () -> new Factory(List.of(new UseByStrategy(), new BestBeforeStrategy()), 14, 7));
    }

    @Test
    void createsTrimmedBatchFromRequest() {
        BatchEntity batch = factory.createBatch(new CreateBatchRequest(
                "  Rice ", " Grains ", 3, " Shelf A ", TODAY, ExpirationDateType.BEST_BEFORE));

        assertEquals("Rice", batch.getProductName());
        assertEquals("Grains", batch.getCategory());
        assertEquals("Shelf A", batch.getStorageLocation());
    }

    @Test
    void classifiesBatchesIntoTiers() {
        assertEquals(ExpirationStatus.EXPIRED, dto(-1, ExpirationDateType.BEST_BEFORE).status());
        assertEquals(ExpirationStatus.RED, dto(0, ExpirationDateType.BEST_BEFORE).status());
        assertEquals(ExpirationStatus.RED, dto(7, ExpirationDateType.BEST_BEFORE).status());
        assertEquals(ExpirationStatus.YELLOW, dto(8, ExpirationDateType.BEST_BEFORE).status());
        assertEquals(ExpirationStatus.YELLOW, dto(14, ExpirationDateType.BEST_BEFORE).status());
        assertEquals(ExpirationStatus.GREEN, dto(15, ExpirationDateType.BEST_BEFORE).status());
        assertEquals(15, dto(15, ExpirationDateType.BEST_BEFORE).daysUntilExpiration());
    }

    @Test
    void marksExpiredUseByBatchesAsNotDispatchable() {
        assertFalse(dto(-1, ExpirationDateType.USE_BY).dispatchable());
        assertTrue(dto(-1, ExpirationDateType.BEST_BEFORE).dispatchable());
    }

    private BatchDTO dto(int daysLeft, ExpirationDateType type) {
        BatchEntity batch = new BatchEntity("Milk", "Dairy", 2, "Fridge", TODAY.plusDays(daysLeft), type);
        return factory.createBatchDto(batch, TODAY);
    }
}
