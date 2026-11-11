package stockdate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class StockdateApplication { // application entry point. Starts Spring Boot and its web server

    public static void main(String[] args) {
        SpringApplication.run(StockdateApplication.class, args);
    }

}
