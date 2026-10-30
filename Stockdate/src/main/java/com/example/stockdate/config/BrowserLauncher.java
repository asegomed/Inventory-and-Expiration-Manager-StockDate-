package com.example.stockdate.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;

@Component
@ConditionalOnProperty(name = "stockdate.browser.open", havingValue = "true", matchIfMissing = true)
public class BrowserLauncher {

    private static final Logger logger = LoggerFactory.getLogger(BrowserLauncher.class);
    private static final URI APPLICATION_URL = URI.create("http://localhost:8080");

    @EventListener(ApplicationReadyEvent.class)
    public void openApplication() {
        if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            logger.info("Abre StockDate en tu navegador: {}", APPLICATION_URL);
            return;
        }

        try {
            Desktop.getDesktop().browse(APPLICATION_URL);
        } catch (IOException exception) {
            logger.warn("No se pudo abrir el navegador. Abre StockDate en: {}", APPLICATION_URL, exception);
        }
    }
}
