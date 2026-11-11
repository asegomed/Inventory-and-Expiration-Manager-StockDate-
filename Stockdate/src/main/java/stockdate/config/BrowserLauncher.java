package stockdate.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
// To open http://localhost:8080 in the default browser once the server has started.
@Component
@ConditionalOnProperty(name = "stockdate.browser.open", havingValue = "true", matchIfMissing = true)
public class BrowserLauncher {

    private static final Logger LOGGER = LoggerFactory.getLogger(BrowserLauncher.class);
    private static final URI APPLICATION_URL = URI.create("http://localhost:8080");

    @EventListener(ApplicationReadyEvent.class)
    public void openApplication() {
        if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            LOGGER.info("Open StockDate in your browser: {}", APPLICATION_URL);
            return;
        }

        try {
            Desktop.getDesktop().browse(APPLICATION_URL);
        } catch (IOException exception) {
            LOGGER.warn("Could not open the browser. Open StockDate at: {}", APPLICATION_URL, exception);
        }
    }
}
