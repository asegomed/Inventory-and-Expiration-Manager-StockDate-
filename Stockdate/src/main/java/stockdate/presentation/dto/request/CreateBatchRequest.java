package com.example.stockdate.presentation.dto.request;

import com.example.stockdate.ExpirationDateType;

import java.time.LocalDate;
// Necesary imports for the different batches
public record CreateBatchRequest(
        String productName,
        String category,
        int quantity,
        String storageLocation,
        LocalDate expirationDate,
        ExpirationDateType expirationDateType
) {
}
