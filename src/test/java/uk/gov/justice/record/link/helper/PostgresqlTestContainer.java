package uk.gov.justice.record.link.helper;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public interface PostgresqlTestContainer {
    @ServiceConnection
    @Container
    PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>(
            "postgres:16-alpine"
    );
}
