package stockdate.service.strategy;

import stockdate.ExpirationDateType;

import java.time.LocalDate;

/**
 * Strategy pattern: the rules that depend on the expiration date type of a batch.
 *
 * <p>A use-by date and a best-before date behave differently: a use-by batch cannot be
 * registered with a past date and cannot be dispatched once expired, while a best-before batch
 * can. Each date type has its own implementation, so adding a new type only needs a new class.
 *
 * <p>Concrete strategies: {@link UseByStrategy} and {@link BestBeforeStrategy}.
 * They are selected by {@link stockdate.service.factory.Factory#createStrategy(ExpirationDateType)}.
 */
public interface Strategy {

    /** Date type handled by this strategy. */
    ExpirationDateType getDateType();

    /**
     * Checks the expiration date of a new or edited batch.
     *
     * @throws IllegalArgumentException if the date is not allowed for this date type
     */
    void validateExpirationDate(LocalDate expirationDate, LocalDate today);

    /** Whether a batch with this expiration date can still be given to beneficiaries. */
    boolean canBeDispatched(LocalDate expirationDate, LocalDate today);
}
