package stockdate.presentation.dto.response;

import stockdate.ExpirationDateType;

import java.time.LocalDate;

// Batch data sent to the web, including the calculated days left, status tier and dispatchability.
public record BatchDTO(
        Long id,
        String productName,
        String category,
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
