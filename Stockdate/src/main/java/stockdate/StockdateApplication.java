package stockdate;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

@SpringBootApplication
@EnableAsync
public class StockdateApplication { // application entry point. Starts Spring Boot and its web server

    private static final String MIGRATION_BEAN = "legacyDatabaseMigration";

    public static void main(String[] args) {
        SpringApplication.run(StockdateApplication.class, args);
    }

    @Bean
    static BeanFactoryPostProcessor entityManagerFactoryDependsOnMigration() {
        return beanFactory -> {
            String entityManagerFactory = "entityManagerFactory";
            if (!beanFactory.containsBeanDefinition(entityManagerFactory)) {
                return;
            }
            BeanDefinition definition = beanFactory.getBeanDefinition(entityManagerFactory);
            Set<String> dependencies = new LinkedHashSet<>();
            String[] existingDependencies = definition.getDependsOn();
            if (existingDependencies != null) {
                dependencies.addAll(Arrays.asList(existingDependencies));
            }
            dependencies.add(MIGRATION_BEAN);
            definition.setDependsOn(dependencies.toArray(String[]::new));
        };
    }

    @Bean(MIGRATION_BEAN)
    static InitializingBean migrateLegacyDatabase(DataSource dataSource) {
        return () -> {
            try (Connection connection = dataSource.getConnection()) {
                migrateLegacyBatchTable(connection);
            }
        };
    }

    private static void migrateLegacyBatchTable(Connection connection) throws SQLException {
        Set<String> columns = new HashSet<>();
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("PRAGMA table_info(batch)")) {
            while (resultSet.next()) {
                columns.add(resultSet.getString("name"));
            }
        }
        if (columns.isEmpty() || columns.contains("category_id")) {
            return;
        }
        if (!columns.contains("category")) {
            throw new SQLException("Cannot migrate batch table: expected a category column.");
        }

        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = OFF");
            statement.execute("PRAGMA legacy_alter_table = ON");
        }

        boolean originalAutoCommit = connection.getAutoCommit();
        try {
            connection.setAutoCommit(false);
            try (Statement statement = connection.createStatement()) {
                statement.execute("""
                        CREATE TABLE IF NOT EXISTS category (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            name TEXT NOT NULL UNIQUE
                        )
                        """);
                statement.execute("""
                        CREATE TABLE batch_new (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            product_name TEXT NOT NULL,
                            category_id INTEGER NOT NULL REFERENCES category(id),
                            quantity INTEGER NOT NULL,
                            storage_location TEXT NOT NULL,
                            expiration_date TEXT NOT NULL,
                            expiration_date_type TEXT NOT NULL
                        )
                        """);
                statement.execute("""
                        INSERT OR IGNORE INTO category (name)
                        SELECT DISTINCT category FROM batch WHERE category IS NOT NULL
                        """);
                statement.execute("""
                        INSERT INTO batch_new (
                            id, product_name, category_id, quantity,
                            storage_location, expiration_date, expiration_date_type
                        )
                        SELECT batch.id, batch.product_name, category.id, batch.quantity,
                               batch.storage_location, batch.expiration_date,
                               batch.expiration_date_type
                        FROM batch
                        JOIN category ON category.name = batch.category
                        """);
                statement.execute("DROP TABLE batch");
                statement.execute("ALTER TABLE batch_new RENAME TO batch");
            }
            connection.commit();
        } catch (SQLException | RuntimeException exception) {
            connection.rollback();
            throw exception;
        } finally {
            connection.setAutoCommit(originalAutoCommit);
            try (Statement statement = connection.createStatement()) {
                statement.execute("PRAGMA legacy_alter_table = OFF");
                statement.execute("PRAGMA foreign_keys = ON");
            }
        }
    }

}
