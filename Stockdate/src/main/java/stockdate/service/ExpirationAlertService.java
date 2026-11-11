package stockdate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import stockdate.persistence.entity.BatchEntity;
import stockdate.persistence.repository.InventoryRepository;
import stockdate.presentation.dto.response.BatchDTO;
import stockdate.presentation.dto.response.BatchDTO.ExpirationStatus;
import stockdate.presentation.dto.response.DashboardDTO;
import stockdate.service.factory.Factory;
import stockdate.service.observer.Observer;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

// Concrete observer: keeps a cached dashboard summary and invalidates it when the stock changes.
@Service
public class ExpirationAlertService implements Observer {

    private final InventoryRepository inventoryRepository;
    private final Factory factory;
    private final Clock clock;

    // Incremented on every stock change; a cached summary is only valid for the version it was built with.
    private final AtomicLong stockVersion = new AtomicLong();
    private final AtomicReference<CachedDashboard> cache = new AtomicReference<>();

    @Autowired
    public ExpirationAlertService(InventoryRepository inventoryRepository, Factory factory) {
        this(inventoryRepository, factory, Clock.systemDefaultZone());
    }

    ExpirationAlertService(InventoryRepository inventoryRepository, Factory factory, Clock clock) {
        this.inventoryRepository = inventoryRepository;
        this.factory = factory;
        this.clock = clock;
    }

    public DashboardDTO getDashboard() {
        LocalDate today = LocalDate.now(clock);
        long version = stockVersion.get();
        CachedDashboard cached = cache.get();
        if (cached != null && cached.isValidFor(version, today)) {
            return cached.dashboard();
        }
        DashboardDTO dashboard = buildDashboard(today);
        cache.set(new CachedDashboard(version, today, dashboard));
        return dashboard;
    }

    @Async
    @Override
    public void onStockChanged() {
        stockVersion.incrementAndGet();
    }

    private DashboardDTO buildDashboard(LocalDate today) {
        Map<ExpirationStatus, Integer> counts = new EnumMap<>(ExpirationStatus.class);
        List<BatchDTO> pinned = new ArrayList<>();
        for (BatchEntity batch : inventoryRepository.findAllByOrderByExpirationDateAscIdAsc()) {
            BatchDTO dto = factory.createBatchDto(batch, today);
            counts.merge(dto.status(), 1, Integer::sum);
            if (dto.status() == ExpirationStatus.EXPIRED || dto.status() == ExpirationStatus.RED) {
                pinned.add(dto);
            }
        }
        return new DashboardDTO(
                counts.getOrDefault(ExpirationStatus.EXPIRED, 0),
                counts.getOrDefault(ExpirationStatus.RED, 0),
                counts.getOrDefault(ExpirationStatus.YELLOW, 0),
                counts.getOrDefault(ExpirationStatus.GREEN, 0),
                List.copyOf(pinned));
    }

    private record CachedDashboard(long version, LocalDate builtOn, DashboardDTO dashboard) {

        boolean isValidFor(long currentVersion, LocalDate today) {
            return version == currentVersion && builtOn.equals(today);
        }
    }
}
