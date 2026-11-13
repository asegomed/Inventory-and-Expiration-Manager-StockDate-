package stockdate.presentation.dto.response;

// JSON body returned with HTTP 400 and 404 errors: {"message": "..."}.
public record ApiError(String message) {
}
