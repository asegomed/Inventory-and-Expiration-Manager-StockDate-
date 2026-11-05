package com.example.stockdate.service;

import com.example.stockdate.ExpirationDateType;
import com.example.stockdate.presentation.dto.request.CreateBatchRequest;
import com.example.stockdate.presentation.dto.response.BatchDTO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;

import static com.example.stockdate.presentation.dto.response.BatchDTO.ExpirationStatus;

// For validating and managing inventory batches in the database
@Service
public class InventoryManager {

    private final JdbcTemplate jdbc;

    public InventoryManager(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<BatchDTO> getBatches() {
        return jdbc.query(
                "SELECT id, product_name, category, quantity, storage_location, "
                        + "expiration_date, expiration_date_type FROM batch "
                        + "ORDER BY expiration_date, id",
                (rs, rowNum) -> toDto(
                        rs.getLong("id"),
                        rs.getString("product_name"),
                        rs.getString("category"),
                        rs.getInt("quantity"),
                        rs.getString("storage_location"),
                        LocalDate.parse(rs.getString("expiration_date")),
                        ExpirationDateType.valueOf(rs.getString("expiration_date_type"))
                )
        );
    }

    public BatchDTO registerBatch(CreateBatchRequest request) {
        validateBatch(request);

        String sql = "INSERT INTO batch (product_name, category, quantity, storage_location, "
                + "expiration_date, expiration_date_type) VALUES (?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, request.productName().trim());
            ps.setString(2, request.category().trim());
            ps.setInt(3, request.quantity());
            ps.setString(4, request.storageLocation().trim());
            ps.setString(5, request.expirationDate().toString());
            ps.setString(6, request.expirationDateType().name());
            return ps;
        }, keyHolder);

        long id = keyHolder.getKey().longValue();
        return toDto(id, request.productName().trim(), request.category().trim(),
                request.quantity(), request.storageLocation().trim(),
                request.expirationDate(), request.expirationDateType());
    }

    public BatchDTO updateBatch(long id, CreateBatchRequest request) {
        validateBatch(request);

        int updatedRows = jdbc.update(
                "UPDATE batch SET product_name = ?, category = ?, quantity = ?, "
                        + "storage_location = ?, expiration_date = ?, expiration_date_type = ? "
                        + "WHERE id = ?",
                request.productName().trim(),
                request.category().trim(),
                request.quantity(),
                request.storageLocation().trim(),
                request.expirationDate().toString(),
                request.expirationDateType().name(),
                id
        );
        if (updatedRows == 0) {
            throw new NoSuchElementException("Batch not found.");
        }

        return toDto(id, request.productName().trim(), request.category().trim(),
                request.quantity(), request.storageLocation().trim(),
                request.expirationDate(), request.expirationDateType());
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

    private BatchDTO toDto(long id, String productName, String category, int quantity,
                           String storageLocation, LocalDate expirationDate,
                           ExpirationDateType type) {
        long days = ChronoUnit.DAYS.between(LocalDate.now(), expirationDate);
        return new BatchDTO(id, productName, category, quantity, storageLocation,
                expirationDate, type, days, getExpirationStatus(days));
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
