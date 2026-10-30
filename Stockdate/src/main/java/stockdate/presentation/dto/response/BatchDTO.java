package com.example.stockdate.presentation.dto.response;

import com.example.stockdate.ExpirationDateType;

import java.time.LocalDate;

public record BatchDTO(
        long id,
        String productName,
        String category,
        int quantity,
        String storageLocation,
        LocalDate expirationDate,
        ExpirationDateType expirationDateType,
        long daysUntilExpiration,
        ExpirationStatus status
) {
    public enum ExpirationStatus {
        EXPIRED,
        RED,
        YELLOW,
        GREEN
    }
}
