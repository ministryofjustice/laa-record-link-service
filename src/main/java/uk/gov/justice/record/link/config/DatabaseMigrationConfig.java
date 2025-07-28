// src/main/java/uk/gov/justice/record.link.config/DatabaseMigrationConfig.java
package uk.gov.justice.record.link.config;

import org.flywaydb.core.Flyway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DatabaseMigrationConfig {

    @Bean
    public Flyway flyway(DataSource dataSource) {
        // Configure Flyway using the provided DataSource and migration locations
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:/db/migration") // Your SQL migration files location
                .load();
        flyway.migrate(); // This line explicitly triggers the migration
        return flyway;
    }
}