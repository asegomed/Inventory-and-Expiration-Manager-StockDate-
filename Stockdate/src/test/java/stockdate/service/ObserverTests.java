package stockdate.service;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import stockdate.ExpirationDateType;
import stockdate.persistence.entity.BatchEntity;
import stockdate.persistence.repository.InventoryRepository;
import stockdate.presentation.dto.request.CreateBatchRequest;
import stockdate.presentation.dto.response.DashboardDTO;
import stockdate.service.factory.Factory;
import stockdate.service.observer.Observer;
import stockdate.service.strategy.BestBeforeStrategy;
import stockdate.service.strategy.UseByStrategy;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ObserverTests {

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 11);

    private final InventoryRepository repository = mock(InventoryRepository.class);
    private final Factory factory = new Factory(List.of(new UseByStrategy(), new BestBeforeStrategy()), 7, 14);
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void subjectNotifiesEveryObserverAfterSavingABatch() {
        Observer first = mock(Observer.class);
        Observer second = mock(Observer.class);
        when(repository.save(any(BatchEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        InventoryManager manager = new InventoryManager(repository, factory, List.of(first, second),
                validator, fixedClock(TODAY));

        manager.registerBatch(request(TODAY.plusDays(30)));

        verify(first, times(1)).onStockChanged();
        verify(second, times(1)).onStockChanged();
    }

    @Test
    void subjectDoesNotNotifyWhenValidationFails() {
        Observer observer = mock(Observer.class);
        InventoryManager manager = new InventoryManager(repository, factory, List.of(observer),
                validator, fixedClock(TODAY));
        CreateBatchRequest invalid = new CreateBatchRequest(
                "Milk", "Dairy", 0, "Fridge", TODAY.plusDays(3), ExpirationDateType.USE_BY);

        assertThrows(IllegalArgumentException.class, () -> manager.registerBatch(invalid));

        verify(observer, never()).onStockChanged();
    }

    @Test
    void dashboardIsCachedUntilTheStockChanges() {
        when(repository.findAllByOrderByExpirationDateAscIdAsc())
                .thenReturn(List.of(batch(-2), batch(3), batch(10), batch(30)));
        ExpirationAlertService service = new ExpirationAlertService(repository, factory, fixedClock(TODAY));

        DashboardDTO first = service.getDashboard();
        DashboardDTO second = service.getDashboard();

        assertSame(first, second);
        verify(repository, times(1)).findAllByOrderByExpirationDateAscIdAsc();
        assertEquals(1, first.expiredCount());
        assertEquals(1, first.redCount());
        assertEquals(1, first.yellowCount());
        assertEquals(1, first.greenCount());
        assertEquals(2, first.pinnedBatches().size());
        assertEquals(3, first.nextExpiring().daysUntilExpiration());

        service.onStockChanged();
        service.getDashboard();

        verify(repository, times(2)).findAllByOrderByExpirationDateAscIdAsc();
    }

    @Test
    void dashboardIsRebuiltWhenTheDateChanges() {
        when(repository.findAllByOrderByExpirationDateAscIdAsc()).thenReturn(List.of(batch(8)));
        Clock clock = mock(Clock.class);
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        when(clock.instant()).thenReturn(TODAY.atStartOfDay(ZoneOffset.UTC).toInstant());
        ExpirationAlertService service = new ExpirationAlertService(repository, factory, clock);

        assertEquals(1, service.getDashboard().yellowCount());

        when(clock.instant()).thenReturn(TODAY.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant());

        assertEquals(1, service.getDashboard().redCount());
    }

    private BatchEntity batch(int daysLeft) {
        return new BatchEntity("Milk", "Dairy", 2, "Fridge", TODAY.plusDays(daysLeft),
                ExpirationDateType.BEST_BEFORE);
    }

    private CreateBatchRequest request(LocalDate expirationDate) {
        return new CreateBatchRequest("Rice", "Grains", 4, "Shelf A", expirationDate,
                ExpirationDateType.BEST_BEFORE);
    }

    private Clock fixedClock(LocalDate date) {
        ZoneId zone = ZoneOffset.UTC;
        return Clock.fixed(date.atStartOfDay(zone).toInstant(), zone);
    }
}
