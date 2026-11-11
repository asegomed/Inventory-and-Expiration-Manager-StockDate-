package stockdate.presentation.dto.response;

import stockdate.ExpirationDateType;

import java.time.LocalDate;

// DTO class for representing batch information in the response (what user sees in the web)
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
