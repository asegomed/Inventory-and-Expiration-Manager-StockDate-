package stockdate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class StockdateApplication { // application entry point. Starts Spring Boot and its web server

    public static void main(String[] args) {
        SpringApplication.run(StockdateApplication.class, args);
    }

}
