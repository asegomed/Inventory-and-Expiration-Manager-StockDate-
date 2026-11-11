package stockdate.service.observer;

/**
 * Observer pattern: contract for components that must react when the stock changes.
 *
 * <p>The subject is {@link stockdate.service.InventoryManager}: after every operation that changes
 * the stock (register, update and, later, dispatch and discard) it calls {@link #onStockChanged()}
 * on every registered observer. The subject only knows this interface, not the concrete observers.
 *
 * <p>Concrete observers: {@link stockdate.service.ExpirationAlertService}.
 */
@SuppressWarnings("PMD.ImplicitFunctionalInterface") // Observer contract, not meant to be a lambda.
public interface Observer {

    /** Called by the subject after a stock change has been saved. */
    void onStockChanged();
}