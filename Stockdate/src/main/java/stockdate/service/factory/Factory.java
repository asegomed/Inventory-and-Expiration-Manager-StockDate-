package stockdate.service.factory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import stockdate.ExpirationDateType;
import stockdate.persistence.entity.BatchEntity;
import stockdate.presentation.dto.request.CreateBatchRequest;
import stockdate.presentation.dto.response.BatchDTO;
import stockdate.presentation.dto.response.BatchDTO.ExpirationStatus;
import stockdate.service.strategy.Strategy;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Factory pattern: the single place where the service layer creates its objects.
 *
 * <ul>
 *     <li>{@link #createStrategy(ExpirationDateType)} returns the {@link Strategy} for a date type.</li>
 *     <li>{@link #createBatch(CreateBatchRequest)} builds a new {@link BatchEntity} from a form.</li>
 *     <li>{@link #createBatchDto(BatchEntity, LocalDate)} builds the {@link BatchDTO} sent to the web,
 *     including days left and the status tier (thresholds read from application.properties).</li>
 * </ul>
 *
 * <p>Used by {@link stockdate.service.InventoryManager} and
 * {@link stockdate.service.ExpirationAlertService}, so both build batches the same way.
 */
@Component
public class Factory {

    private final Map<ExpirationDateType, Strategy> strategies = new EnumMap<>(ExpirationDateType.class);
    private final int redDays;
    private final int yellowDays;

    public Factory(List<Strategy> strategyList,
                   @Value("${stockdate.alert.red-days:7}") int redDays,
                   @Value("${stockdate.alert.yellow-days:14}") int yellowDays) {
        for (Strategy strategy : strategyList) {
            strategies.put(strategy.getDateType(), strategy);
        }
        for (ExpirationDateType type : ExpirationDateType.values()) {
            if (!strategies.containsKey(type)) {
                throw new IllegalStateException("No strategy registered for date type " + type);
            }
        }
        if (redDays < 0 || yellowDays <= redDays) {
            throw new IllegalStateException("Alert thresholds must satisfy 0 <= red-days < yellow-days.");
        }
        this.redDays = redDays;
        this.yellowDays = yellowDays;
    }

    public Strategy createStrategy(ExpirationDateType type) {
        return strategies.get(type);
    }

    public BatchEntity createBatch(CreateBatchRequest request) {
        return new BatchEntity(request.productName().trim(), request.category().trim(), request.quantity(),
                request.storageLocation().trim(), request.expirationDate(), request.expirationDateType());
    }

    public BatchDTO createBatchDto(BatchEntity batch, LocalDate today) {
        long days = ChronoUnit.DAYS.between(today, batch.getExpirationDate());
        boolean dispatchable = createStrategy(batch.getExpirationDateType())
                .canBeDispatched(batch.getExpirationDate(), today);
        return new BatchDTO(batch.getId(), batch.getProductName(), batch.getCategory(), batch.getQuantity(),
                batch.getStorageLocation(), batch.getExpirationDate(), batch.getExpirationDateType(),
                days, classify(days), dispatchable);
    }

    private ExpirationStatus classify(long daysUntilExpiration) {
        if (daysUntilExpiration < 0) {
            return ExpirationStatus.EXPIRED;
        }
        if (daysUntilExpiration <= redDays) {
            return ExpirationStatus.RED;
        }
        if (daysUntilExpiration <= yellowDays) {
            return ExpirationStatus.YELLOW;
        }
        return ExpirationStatus.GREEN;
    }
}
