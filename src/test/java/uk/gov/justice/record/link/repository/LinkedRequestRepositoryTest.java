package uk.gov.justice.record.link.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import uk.gov.justice.record.link.entity.Status;
import uk.gov.justice.record.link.helper.PostgresqlTestContainer;
import uk.gov.justice.record.link.respository.LinkedRequestRepository;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ImportTestcontainers(PostgresqlTestContainer.class)
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
public class LinkedRequestRepositoryTest {
    @Autowired
    private LinkedRequestRepository linkedRequestRepository;

    @Nested
    @Sql("classpath:test_data/insert_link_request.sql")
    class CountByLoginIdAndStatusNotIn {

        @DisplayName("should return count of 1 when status in APPROVED")
        @Test
        void shouldFindByLoginIdAndStatusNotApproved() {
            var actualResults = linkedRequestRepository
                    .countByCcmsUser_LoginIdAndStatusIn("123", List.of(Status.OPEN, Status.APPROVED));
            assertThat(actualResults).isEqualTo(1);
        }

        @DisplayName("Should return count of 1 when status in OPEN")
        @Test
        void shouldFindByLoginIdAndStatusNotInOpen() {
            var actualResults = linkedRequestRepository
                    .countByCcmsUser_LoginIdAndStatusIn("456", List.of(Status.OPEN, Status.APPROVED));
            assertThat(actualResults).isEqualTo(1);
        }

        @DisplayName("Should return count of 0 when status in REJECTED")
        @Test
        void shouldFindByLoginIdAndStatusNotInRejected() {
            var actualResults = linkedRequestRepository
                    .countByCcmsUser_LoginIdAndStatusIn("678", List.of(Status.OPEN, Status.APPROVED));
            assertThat(actualResults).isEqualTo(0);
        }
    }

}
