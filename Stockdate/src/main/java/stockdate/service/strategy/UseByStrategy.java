package stockdate.service.strategy;

import org.springframework.stereotype.Component;
import stockdate.ExpirationDateType;

import java.time.LocalDate;

/** Use-by dates are a safety limit: never in the past and never dispatched once expired. */
@Component
public class UseByStrategy implements Strategy {

    @Override
    public ExpirationDateType getDateType() {
        return ExpirationDateType.USE_BY;
    }

    @Override
    public void validateExpirationDate(LocalDate expirationDate, LocalDate today) {
        if (expirationDate.isBefore(today)) {
            throw new IllegalArgumentException("A use-by date cannot be in the past.");
        }
    }

    @Override
    public boolean canBeDispatched(LocalDate expirationDate, LocalDate today) {
        return !expirationDate.isBefore(today);
    }
}
