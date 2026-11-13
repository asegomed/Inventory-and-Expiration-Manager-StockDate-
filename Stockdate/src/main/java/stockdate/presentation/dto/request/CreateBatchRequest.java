package stockdate.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import stockdate.ExpirationDateType;

import java.time.LocalDate;

// Data sent by the web form to register or edit a batch. The annotations are the field rules (UC-02, UC-03).
public record CreateBatchRequest(
        @NotBlank(message = "Product name is required.") String productName,
        @NotBlank(message = "Category is required.") String category,
        @Positive(message = "Quantity must be greater than zero.") int quantity,
        @NotBlank(message = "Storage location is required.") String storageLocation,
        @NotNull(message = "Date and date type are required.") LocalDate expirationDate,
        @NotNull(message = "Date and date type are required.") ExpirationDateType expirationDateType
) {

    // Same data with surrounding spaces removed from the text fields.
    public CreateBatchRequest trimmed() {
        return new CreateBatchRequest(productName.trim(), category.trim(), quantity, storageLocation.trim(),
                expirationDate, expirationDateType);
    }
}
