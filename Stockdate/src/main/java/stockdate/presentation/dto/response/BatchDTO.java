package stockdate.presentation.dto.response;

import stockdate.ExpirationDateType;

import java.time.LocalDate;

// Batch data sent to the web, including calculated expiration information.
public record BatchDTO(
        Long id,
        String productName,
        String category,
        Long categoryId,
        int quantity,
        String storageLocation,
        LocalDate expirationDate,
        ExpirationDateType expirationDateType,
        long daysUntilExpiration,
        ExpirationStatus status,
        boolean dispatchable
) {
    public enum ExpirationStatus {
        EXPIRED,
        RED,
        YELLOW,
        GREEN
    }
}
