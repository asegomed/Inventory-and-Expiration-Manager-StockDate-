package stockdate.service.strategy;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StrategyTests {

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 11);

    private final Strategy useBy = new UseByStrategy();
    private final Strategy bestBefore = new BestBeforeStrategy();

    @Test
    void useByRejectsPastDates() {
        assertThrows(IllegalArgumentException.class,
                () -> useBy.validateExpirationDate(TODAY.minusDays(1), TODAY));
        assertDoesNotThrow(() -> useBy.validateExpirationDate(TODAY, TODAY));
    }

    @Test
    void useByCannotBeDispatchedOnceExpired() {
        assertTrue(useBy.canBeDispatched(TODAY, TODAY));
        assertFalse(useBy.canBeDispatched(TODAY.minusDays(1), TODAY));
    }

    @Test
    void bestBeforeAcceptsPastDatesAndCanBeDispatched() {
        assertDoesNotThrow(() -> bestBefore.validateExpirationDate(TODAY.minusDays(10), TODAY));
        assertTrue(bestBefore.canBeDispatched(TODAY.minusDays(10), TODAY));
    }
}
