package com.example.stockdate.service;

import com.example.stockdate.ExpirationDateType;
import com.example.stockdate.presentation.dto.request.CreateBatchRequest;
import com.example.stockdate.presentation.dto.response.BatchDTO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static com.example.stockdate.presentation.dto.response.BatchDTO.ExpirationStatus;

@Service
public class InventoryManager {

    private final AtomicLong nextId = new AtomicLong(1);
    private final ConcurrentHashMap<Long, BatchDTO> batches = new ConcurrentHashMap<>();

    public List<BatchDTO> getBatches() {
        return batches.values().stream()
                .sorted(Comparator.comparing(BatchDTO::expirationDate).thenComparingLong(BatchDTO::id))
                .toList();
    }

    public BatchDTO registerBatch(CreateBatchRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Los datos del lote son obligatorios.");
        }
        if (request.productName() == null || request.productName().isBlank()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio.");
        }
        if (request.category() == null || request.category().isBlank()) {
            throw new IllegalArgumentException("La categoria es obligatoria.");
        }
        if (request.quantity() <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
        }
        if (request.storageLocation() == null || request.storageLocation().isBlank()) {
            throw new IllegalArgumentException("La ubicacion es obligatoria.");
        }
        if (request.expirationDate() == null || request.expirationDateType() == null) {
            throw new IllegalArgumentException("La fecha y su tipo son obligatorios.");
        }
        if (request.expirationDateType() == ExpirationDateType.USE_BY
                && request.expirationDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de caducidad no puede estar en el pasado.");
        }

        LocalDate today = LocalDate.now();
        long daysUntilExpiration = ChronoUnit.DAYS.between(today, request.expirationDate());
        ExpirationStatus status = getExpirationStatus(daysUntilExpiration);
        BatchDTO batch = new BatchDTO(
                nextId.getAndIncrement(),
                request.productName().trim(),
                request.category().trim(),
                request.quantity(),
                request.storageLocation().trim(),
                request.expirationDate(),
                request.expirationDateType(),
                daysUntilExpiration,
                status
        );
        batches.put(batch.id(), batch);
        return batch;
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
