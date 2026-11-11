package stockdate;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "stockdate.browser.open=false",
        "spring.datasource.url=jdbc:sqlite::memory:"
})
class StockdateApplicationTests {

    @Test
    void contextLoads() {
    }

}
