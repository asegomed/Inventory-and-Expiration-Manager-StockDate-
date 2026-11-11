package com.example.stockdate.service;

import com.example.stockdate.ExpirationDateType;
import com.example.stockdate.persistence.entity.BatchEntity;
import com.example.stockdate.persistence.repository.InventoryRepository;
import com.example.stockdate.presentation.dto.request.CreateBatchRequest;
import com.example.stockdate.presentation.dto.response.BatchDTO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import static com.example.stockdate.presentation.dto.response.BatchDTO.ExpirationStatus;

// For validating and managing inventory batches in the database
@Service
public class InventoryManager {

    private final InventoryRepository inventoryRepository;

    public InventoryManager(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public List<BatchDTO> getBatches() {
        return inventoryRepository.findAllByOrderByExpirationDateAscIdAsc().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public BatchDTO registerBatch(CreateBatchRequest request) {
        validateBatch(request);
        return toDto(inventoryRepository.save(toEntity(request)));
    }

    public BatchDTO updateBatch(long id, CreateBatchRequest request) {
        validateBatch(request);

        BatchEntity batch = inventoryRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Batch not found."));
        batch.update(trim(request.productName()), trim(request.category()), request.quantity(),
                trim(request.storageLocation()), request.expirationDate(), request.expirationDateType());
        return toDto(inventoryRepository.save(batch));
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
        if (request.expirationDateType() == ExpirationDateType.USE_BY
                && request.expirationDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("The expiration date cannot be in the past.");
        }
    }

    private BatchEntity toEntity(CreateBatchRequest request) {
        return new BatchEntity(trim(request.productName()), trim(request.category()), request.quantity(),
                trim(request.storageLocation()), request.expirationDate(), request.expirationDateType());
    }

    private BatchDTO toDto(BatchEntity batch) {
        long days = ChronoUnit.DAYS.between(LocalDate.now(), batch.getExpirationDate());
        return new BatchDTO(batch.getId(), batch.getProductName(), batch.getCategory(), batch.getQuantity(),
                batch.getStorageLocation(), batch.getExpirationDate(), batch.getExpirationDateType(),
                days, getExpirationStatus(days));
    }

    private String trim(String value) {
        return value.trim();
    }

    private ExpirationStatus getExpirationStatus(long daysUntilExpiration) {
        if (daysUntilExpiration < 0) {
            return ExpirationStatus.EXPIRED;
        }
        if (daysUntilExpiration <= 7) {
            return ExpirationStatus.RED;
        }
        if (daysUntilExpiration <= 14) {
            return ExpirationStatus.YELLOW;
        }
        return ExpirationStatus.GREEN;
    }
}
