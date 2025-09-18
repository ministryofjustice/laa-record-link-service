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
import uk.gov.justice.record.link.helper.PostgresqlTestContainer;
import uk.gov.justice.record.link.respository.CcmsUserRepository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ImportTestcontainers(PostgresqlTestContainer.class)
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
public class CcmsUserRepositoryTest {
    @Autowired
    private CcmsUserRepository ccmsUserRepository;

    @Nested
    class FindByLoginId {
        @Sql("classpath:test_data/insert_link_request.sql")
        @DisplayName("Should return CCMS users for login id that exits in CCMS table ")
        @Test
        void returnCcmsUsersForValidLoginId() {
            var actualResult = ccmsUserRepository.findByLoginId("123").orElse(null);

            assertThat(actualResult).isNotNull();
        }

        @Sql("classpath:test_data/insert_link_request.sql")
        @DisplayName("Should return null for login id that does not exits in CCMS table")
        @Test
        void returnNullForInvalidLoginId() {
            var actualResult = ccmsUserRepository.findByLoginId("invalid").orElse(null);

            assertThat(actualResult).isNull();
        }
    }

    @Nested
    class ExistsByFirmCode {
        @Sql("classpath:test_data/insert_link_request.sql")
        @DisplayName("Should return true for firm code that exists in CCMS table")
        @Test
        void returnTrueForValidFirmCode() {
            var actualResult = ccmsUserRepository.existsByFirmCode("F123");

            assertThat(actualResult).isTrue();
        }

        @Sql("classpath:test_data/insert_link_request.sql")
        @DisplayName("Should return false for firm code that does not exist in CCMS table")
        @Test
        void returnFalseForInvalidFirmCode() {
            var actualResult = ccmsUserRepository.existsByFirmCode("invalid");

            assertThat(actualResult).isFalse();
        }
    }

    @Nested
    @Sql("classpath:test_data/insert_link_request.sql")
    class ExistsByLoginIdAndFirmCode {
        @DisplayName("Should return true for when firm code exits for a given login id")
        @Test
        void shouldReturnTrueForValidLoginIdAndFirmCode() {
            var actualResult = ccmsUserRepository.existsByLoginIdAndFirmCode("123", "F123");

            assertThat(actualResult).isTrue();
        }

        @DisplayName("Should return false when firm code does not exits for a given login id")
        @Test
        void shouldReturnFalseForInvalidLoginIdAndFirmCode() {
            var actualResult = ccmsUserRepository.existsByLoginIdAndFirmCode("123", "invalid");

            assertThat(actualResult).isFalse();
        }
    }
}
