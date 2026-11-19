package stockdate.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import stockdate.ExpirationDateType;

import java.time.LocalDate;

// Data sent by the web form to register or edit a batch.
public record CreateBatchRequest(
        @NotBlank(message = "Product name is required.") String productName,
        @NotNull(message = "Category is required.")
        @Positive(message = "Category ID must be positive.") Long categoryId,
        @Positive(message = "Quantity must be greater than zero.") int quantity,
        @NotBlank(message = "Storage location is required.") String storageLocation,
        @NotNull(message = "Date and date type are required.") LocalDate expirationDate,
        @NotNull(message = "Date and date type are required.") ExpirationDateType expirationDateType
) {

    public CreateBatchRequest trimmed() {
        return new CreateBatchRequest(productName.trim(), categoryId, quantity, storageLocation.trim(),
                expirationDate, expirationDateType);
    }
}
