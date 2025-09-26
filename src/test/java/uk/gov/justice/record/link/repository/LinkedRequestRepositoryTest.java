package uk.gov.justice.record.link.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    @Nested
    @Sql("classpath:test_data/insert_link_request_1.sql")
    class FindByOldLoginIdContainsIgnoreCase {
        final Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.asc("createdDate")));

        @DisplayName("Should return all rows when old login id is null")
        @Test
         void oldLoginIdIsNull() {
            var actualResults = linkedRequestRepository.findByOldLoginIdContainingAllIgnoreCase("", pageable);

            assertThat(actualResults.getContent().size()).isEqualTo(4);

            assertThat(actualResults.getTotalElements()).isEqualTo(4);
        }

        @DisplayName("Should return 2 row when old login id start with  user1 (Partial Match)")
        @Test
        void shouldUser1() {
            var actualResults = linkedRequestRepository.findByOldLoginIdContainingAllIgnoreCase("user1", pageable);

            assertThat(actualResults.getContent().size()).isEqualTo(3);
        }

        @DisplayName("Should return 1 row when old login id start with upper case USER2 (Partial Match & Upper Case)")
        @Test
        void shouldUser2() {
            var actualResults = linkedRequestRepository.findByOldLoginIdContainingAllIgnoreCase("USER2", pageable);

            assertThat(actualResults.getContent().size()).isEqualTo(1);
        }

        @DisplayName("Should return 0 row when old login id start with user3 (Partial Match & No match found)")
        @Test
        void shouldUser3() {
            var actualResults = linkedRequestRepository.findByOldLoginIdContainingAllIgnoreCase("user3", pageable);

            assertThat(actualResults.getContent().size()).isEqualTo(0);
        }

        @DisplayName("Should return 4 row when old login id has firm (Partial Match)")
        @Test
        void shouldFirm() {
            var actualResults = linkedRequestRepository.findByOldLoginIdContainingAllIgnoreCase("Firm", pageable);

            assertThat(actualResults.getContent().size()).isEqualTo(4);
        }

        @DisplayName("Should return 2 row when old login id  has firmA (Partial Match)")
        @Test
        void shouldFirmA() {
            var actualResults = linkedRequestRepository.findByOldLoginIdContainingAllIgnoreCase("firmA", pageable);

            assertThat(actualResults.getContent().size()).isEqualTo(2);
        }

        @DisplayName("Should return 2 rows when login id is equal to user1@FirmA.com")
        @Test
        void shouldUserAfirmA() {
            var actualResults = linkedRequestRepository.findByOldLoginIdContainingAllIgnoreCase("user1@FirmA.com", pageable);

            assertThat(actualResults.getContent().size()).isEqualTo(2);
        }

        @DisplayName("Should return 2 rows when login id equals to user2@FirmB.com")
        @Test
        void shouldUserFirmB() {
            var actualResults = linkedRequestRepository.findByOldLoginIdContainingAllIgnoreCase("user2@Firmb.com", pageable);

            assertThat(actualResults.getContent().size()).isEqualTo(1);
        }

        @DisplayName("Should return 2 rows for login id equals to UsEr2@FirmB.com (mixed case)")
        @Test
        void shouldUsEr2FirmB() {
            var actualResults = linkedRequestRepository.findByOldLoginIdContainingAllIgnoreCase("UsEr2@FirmB.com", pageable);

            assertThat(actualResults.getContent().size()).isEqualTo(1);

        }

        @DisplayName("Should return 0 rows for login id equals to FirmC (No Match)")
        @Test
        void shouldFirmC() {
            var actualResults = linkedRequestRepository.findByOldLoginIdContainingAllIgnoreCase("FirmC", pageable);

            assertThat(actualResults.hasNext()).isFalse();
            assertThat(actualResults.hasPrevious()).isFalse();
            assertThat(actualResults.getTotalPages()).isEqualTo(0);
            assertThat(actualResults.getTotalElements()).isEqualTo(0);
            assertThat(actualResults.getContent().size()).isEqualTo(0);

        }

    }

    @Nested
    @Sql("classpath:test_data/insert_assigned_requests.sql")
    @DisplayName("FindByLaaAssignee")
    class FindByLaaAssignee {
        final Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.asc("createdDate")));

        @DisplayName("Should return requests assigned to specific user")
        @Test
        void shouldFindRequestsAssignedToUser() {
            var actualResults = linkedRequestRepository.findByLaaAssignee("testUser1", pageable);

            assertThat(actualResults.getContent().size()).isEqualTo(3);
            assertThat(actualResults.getTotalElements()).isEqualTo(3);
            assertThat(actualResults.getTotalPages()).isEqualTo(1);
        }

        @DisplayName("Should return empty results for non-existent assignee")
        @Test
        void shouldReturnEmptyForNonExistentAssignee() {
            var actualResults = linkedRequestRepository.findByLaaAssignee("nonExistentUser", pageable);

            assertThat(actualResults.getContent().size()).isEqualTo(0);
            assertThat(actualResults.getTotalElements()).isEqualTo(0);
            assertThat(actualResults.getTotalPages()).isEqualTo(0);
            assertThat(actualResults.hasNext()).isFalse();
            assertThat(actualResults.hasPrevious()).isFalse();
        }

        @DisplayName("Should handle pagination correctly")
        @Test
        void shouldHandlePaginationCorrectly() {
            Pageable smallPageable = PageRequest.of(0, 2, Sort.by(Sort.Order.asc("createdDate")));
            var actualResults = linkedRequestRepository.findByLaaAssignee("testUser1", smallPageable);

            assertThat(actualResults.getContent().size()).isEqualTo(2);
            assertThat(actualResults.getTotalElements()).isEqualTo(3);
            assertThat(actualResults.getTotalPages()).isEqualTo(2);
            assertThat(actualResults.hasNext()).isTrue();
            assertThat(actualResults.hasPrevious()).isFalse();
        }

        @DisplayName("Should handle second page of pagination")
        @Test
        void shouldHandleSecondPageOfPagination() {
            Pageable secondPageable = PageRequest.of(1, 2, Sort.by(Sort.Order.asc("createdDate")));
            var actualResults = linkedRequestRepository.findByLaaAssignee("testUser1", secondPageable);

            assertThat(actualResults.getContent().size()).isEqualTo(1);
            assertThat(actualResults.getTotalElements()).isEqualTo(3);
            assertThat(actualResults.getTotalPages()).isEqualTo(2);
            assertThat(actualResults.hasNext()).isFalse();
            assertThat(actualResults.hasPrevious()).isTrue();
        }

        @DisplayName("Should respect sorting by created date")
        @Test
        void shouldSortByCreatedDate() {
            var actualResults = linkedRequestRepository.findByLaaAssignee("testUser1", pageable);

            var content = actualResults.getContent();
            assertThat(content.size()).isGreaterThan(1);
            
            for (int i = 0; i < content.size() - 1; i++) {
                assertThat(content.get(i).getCreatedDate())
                    .isBeforeOrEqualTo(content.get(i + 1).getCreatedDate());
            }
        }
    }

    @Nested
    @Sql("classpath:test_data/insert_unassigned_requests.sql")
    @DisplayName("FindFirstByLaaAssigneeIsNullAndStatusOrderByCreatedDateAsc")
    class FindFirstUnassignedByStatusOrderByCreatedDateAsc {

        @DisplayName("Should return oldest unassigned OPEN request")
        @Test
        void shouldReturnOldestUnassignedOpenRequest() {
            var result = linkedRequestRepository.findFirstByLaaAssigneeIsNullAndStatusOrderByCreatedDateAsc(Status.OPEN);

            assertThat(result).isPresent();
            assertThat(result.get().getLaaAssignee()).isNull();
            assertThat(result.get().getStatus()).isEqualTo(Status.OPEN);
            assertThat(result.get().getOldLoginId()).isEqualTo("oldest_unassigned");
        }

        @DisplayName("Should return empty when no unassigned OPEN requests exist")
        @Test
        void shouldReturnEmptyWhenNoUnassignedOpenRequests() {
            var result = linkedRequestRepository.findFirstByLaaAssigneeIsNullAndStatusOrderByCreatedDateAsc(Status.APPROVED);

            assertThat(result).isEmpty();
        }

        @DisplayName("Should ignore assigned requests even if they are OPEN")
        @Test
        void shouldIgnoreAssignedRequests() {
            var result = linkedRequestRepository.findFirstByLaaAssigneeIsNullAndStatusOrderByCreatedDateAsc(Status.OPEN);

            assertThat(result).isPresent();
            assertThat(result.get().getLaaAssignee()).isNull();
        }
    }

    @Nested
    @Sql("classpath:test_data/insert_assigned_requests.sql")
    @DisplayName("CountByOldLoginIdAndIdamFirmCodeAndStatusIn")
    class CountByOldLoginIdAndIdamFirmCodeAndStatusIn {

        @DisplayName("Should return count as 1 for already linked user with firm")
        @Test
        void shouldReturnCountOfUserAssignedToFirm() {
            var count = linkedRequestRepository.countByOldLoginIdAndIdamFirmCodeAndStatusIn("oldUser2", "TFB002", List.of(Status.APPROVED));
            assertThat(count).isEqualTo(1);
        }

        @DisplayName("Should return zero count for non-existent assignee")
        @Test
        void shouldReturnZeroForNonExistentAssignee() {
            var count = linkedRequestRepository.countByOldLoginIdAndIdamFirmCodeAndStatusIn("noExist", "TFB002", List.of(Status.APPROVED));
            assertThat(count).isEqualTo(0);
        }

        @DisplayName("Should return zero count for existing assignee and different firm code")
        @Test
        void shouldReturnZeroForExistentAssigneeOfDifferentFirm() {
            var count = linkedRequestRepository.countByOldLoginIdAndIdamFirmCodeAndStatusIn("oldUser2", "TFC003", List.of(Status.APPROVED));
            assertThat(count).isEqualTo(0);
        }

        @DisplayName("Should return zero count for non-existent firm")
        @Test
        void shouldReturnZeroForAssigneeOfNonExistFirm() {
            var count = linkedRequestRepository.countByOldLoginIdAndIdamFirmCodeAndStatusIn("oldUser6", "noExist", List.of(Status.APPROVED));
            assertThat(count).isEqualTo(0);
        }
    }

    @Nested
    @Sql("classpath:test_data/insert_unassigned_requests.sql")
    @DisplayName("CountByOldLoginIdAndStatus")
    class CountByOldLoginIdAndStatus {

        @DisplayName("Should return count of 1 for existing OPEN request by oldLoginId")
        @Test
        void shouldReturnCountForExistingOpenRequest() {
            var count = linkedRequestRepository.countByOldLoginIdAndStatus("oldest_unassigned", Status.OPEN);
            assertThat(count).isEqualTo(1);
        }

        @DisplayName("Should return count of 0 for non-existent oldLoginId")
        @Test
        void shouldReturnZeroForNonExistentOldLoginId() {
            var count = linkedRequestRepository.countByOldLoginIdAndStatus("non_existent_login", Status.OPEN);
            assertThat(count).isEqualTo(0);
        }

        @DisplayName("Should return count of 0 for existing oldLoginId with different status")
        @Test
        void shouldReturnZeroForExistingOldLoginIdWithDifferentStatus() {
            var count = linkedRequestRepository.countByOldLoginIdAndStatus("oldest_unassigned", Status.APPROVED);
            assertThat(count).isEqualTo(0);
        }

        @DisplayName("Should return count of 0 for REJECTED status")
        @Test
        void shouldReturnZeroForRejectedStatus() {
            var count = linkedRequestRepository.countByOldLoginIdAndStatus("oldest_unassigned", Status.REJECTED);
            assertThat(count).isEqualTo(0);
        }
    }

}
