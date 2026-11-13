package stockdate.service;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;
import stockdate.persistence.entity.BatchEntity;
import stockdate.persistence.repository.InventoryRepository;
import stockdate.presentation.dto.request.CreateBatchRequest;
import stockdate.presentation.dto.response.BatchDTO;
import stockdate.service.factory.Factory;
import stockdate.service.observer.Observer;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

// Validates and manages inventory batches. Subject of the Observer pattern: notifies stock changes.
@Service
public class InventoryManager {

    private final InventoryRepository inventoryRepository;
    private final Factory factory;
    private final List<Observer> observers;
    private final Validator validator;
    private final Clock clock;

    public InventoryManager(InventoryRepository inventoryRepository, Factory factory, List<Observer> observers,
                            Validator validator, Clock clock) {
        this.inventoryRepository = inventoryRepository;
        this.factory = factory;
        this.observers = List.copyOf(observers);
        this.validator = validator;
        this.clock = clock;
    }

    public List<BatchDTO> getBatches() {
        LocalDate today = LocalDate.now(clock);
        return inventoryRepository.findAllByOrderByExpirationDateAscIdAsc().stream()
                .map(batch -> factory.createBatchDto(batch, today))
                .toList();
    }

    public BatchDTO registerBatch(CreateBatchRequest request) {
        LocalDate today = LocalDate.now(clock);
        validateBatch(request, today);
        BatchEntity saved = inventoryRepository.save(factory.createBatch(request));
        notifyObservers();
        return factory.createBatchDto(saved, today);
    }

    public BatchDTO updateBatch(long id, CreateBatchRequest request) {
        LocalDate today = LocalDate.now(clock);
        validateBatch(request, today);

        BatchEntity batch = inventoryRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Batch not found."));
        CreateBatchRequest data = request.trimmed();
        batch.update(data.productName(), data.category(), data.quantity(),
                data.storageLocation(), data.expirationDate(), data.expirationDateType());
        BatchEntity saved = inventoryRepository.save(batch);
        notifyObservers();
        return factory.createBatchDto(saved, today);
    }

    private void notifyObservers() {
        for (Observer observer : observers) {
            observer.onStockChanged();
        }
    }

    // Field rules come from the annotations on CreateBatchRequest; the date rule depends on its type (Strategy).
    private void validateBatch(CreateBatchRequest request, LocalDate today) {
        if (request == null) {
            throw new IllegalArgumentException("Batch data is required.");
        }
        String errors = validator.validate(request).stream()
                .map(ConstraintViolation::getMessage)
                .distinct()
                .sorted()
                .collect(Collectors.joining(" "));
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(errors);
        }
        factory.createStrategy(request.expirationDateType())
                .validateExpirationDate(request.expirationDate(), today);
    }
}
