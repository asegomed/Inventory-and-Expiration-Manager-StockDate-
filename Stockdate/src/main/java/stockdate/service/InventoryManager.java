package stockdate.service;

import org.springframework.stereotype.Service;
import stockdate.persistence.entity.BatchEntity;
import stockdate.persistence.repository.InventoryRepository;
import stockdate.presentation.dto.request.CreateBatchRequest;
import stockdate.presentation.dto.response.BatchDTO;
import stockdate.service.factory.Factory;
import stockdate.service.observer.Observer;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

// Validates and manages inventory batches. Subject of the Observer pattern: notifies stock changes.
@Service
public class InventoryManager {

    private final InventoryRepository inventoryRepository;
    private final Factory factory;
    private final List<Observer> observers;

    public InventoryManager(InventoryRepository inventoryRepository, Factory factory, List<Observer> observers) {
        this.inventoryRepository = inventoryRepository;
        this.factory = factory;
        this.observers = List.copyOf(observers);
    }

    public List<BatchDTO> getBatches() {
        LocalDate today = LocalDate.now();
        return inventoryRepository.findAllByOrderByExpirationDateAscIdAsc().stream()
                .map(batch -> factory.createBatchDto(batch, today))
                .toList();
    }

    public BatchDTO registerBatch(CreateBatchRequest request) {
        validateBatch(request);
        BatchEntity saved = inventoryRepository.save(factory.createBatch(request));
        notifyObservers();
        return factory.createBatchDto(saved, LocalDate.now());
    }

    public BatchDTO updateBatch(long id, CreateBatchRequest request) {
        validateBatch(request);

        BatchEntity batch = inventoryRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Batch not found."));
        batch.update(request.productName().trim(), request.category().trim(), request.quantity(),
                request.storageLocation().trim(), request.expirationDate(), request.expirationDateType());
        BatchEntity saved = inventoryRepository.save(batch);
        notifyObservers();
        return factory.createBatchDto(saved, LocalDate.now());
    }

    private void notifyObservers() {
        for (Observer observer : observers) {
            observer.onStockChanged();
        }
    }

    private void validateBatch(CreateBatchRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Batch data is required.");
        }
        if (request.productName() == null || request.productName().isBlank()) {
            throw new IllegalArgumentException("Product name is required.");
        }
        if (request.category() == null || request.category().isBlank()) {
            throw new IllegalArgumentException("Category is required.");
        }
        if (request.quantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }
        if (request.storageLocation() == null || request.storageLocation().isBlank()) {
            throw new IllegalArgumentException("Storage location is required.");
        }
        if (request.expirationDate() == null || request.expirationDateType() == null) {
            throw new IllegalArgumentException("Date and date type are required.");
        }
        factory.createStrategy(request.expirationDateType())
                .validateExpirationDate(request.expirationDate(), LocalDate.now());
    }
}
