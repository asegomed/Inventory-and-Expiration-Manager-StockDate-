package stockdate.service.strategy;

import org.springframework.stereotype.Component;
import stockdate.ExpirationDateType;

import java.time.LocalDate;

/** Best-before dates are about quality: past dates are allowed and the food can still be dispatched. */
@Component
public class BestBeforeStrategy implements Strategy {

    @Override
    public ExpirationDateType getDateType() {
        return ExpirationDateType.BEST_BEFORE;
    }

    @Override
    public void validateExpirationDate(LocalDate expirationDate, LocalDate today) {
        // Any date is accepted. The user confirms in the form that the product is still fine (UC-02, 4c).
    }

    @Override
    public boolean canBeDispatched(LocalDate expirationDate, LocalDate today) {
        return true;
    }
}