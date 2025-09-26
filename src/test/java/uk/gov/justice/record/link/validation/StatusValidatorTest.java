package uk.gov.justice.record.link.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.record.link.entity.Status;
import uk.gov.justice.record.link.respository.LinkedRequestRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StatusValidatorTest {
    @Mock
    private LinkedRequestRepository mockLinkedRequestRepository;
    @Mock
    private ConstraintValidatorContext mockConstraintValidatorContext;
    @InjectMocks
    private StatusValidator statusValidator;
    @Captor
    private ArgumentCaptor<String> loginIdCaptor;
    @Captor
    private ArgumentCaptor<List<Status>> statusCaptor;

    @Nested
    class IsValid {
        @DisplayName("Should return false when login id does not match the search criteria")
        @Test
        void loginValid() {
            when(mockLinkedRequestRepository.countByCcmsUser_LoginIdAndStatusIn(eq("user2"),  anyList()))
                    .thenReturn(0);
            when(mockLinkedRequestRepository.countByOldLoginIdAndStatus(eq("user2"),  eq(Status.OPEN)))
                    .thenReturn(0);

            var actualResult = statusValidator.isValid("user2", mockConstraintValidatorContext);

            assertThat(actualResult).isTrue();
            verify(mockLinkedRequestRepository).countByCcmsUser_LoginIdAndStatusIn(loginIdCaptor.capture(), statusCaptor.capture());
            assertThat(statusCaptor.getValue()).containsExactlyInAnyOrder(Status.OPEN, Status.APPROVED);
            assertThat(loginIdCaptor.getValue()).isEqualTo("user2");
        }

        @DisplayName("Should return true when login id does match the search criteria")
        @Test
        void loginInvalid() {
            when(mockLinkedRequestRepository.countByCcmsUser_LoginIdAndStatusIn(eq("user1"),  anyList()))
                    .thenReturn(1);
            when(mockLinkedRequestRepository.countByOldLoginIdAndStatus(eq("user1"),  eq(Status.OPEN)))
                    .thenReturn(0);

            var actualResult = statusValidator.isValid("user1", mockConstraintValidatorContext);

            assertThat(actualResult).isFalse();
            verify(mockLinkedRequestRepository).countByCcmsUser_LoginIdAndStatusIn(loginIdCaptor.capture(), statusCaptor.capture());
            assertThat(statusCaptor.getValue()).containsExactlyInAnyOrder(Status.OPEN, Status.APPROVED);
            assertThat(loginIdCaptor.getValue()).isEqualTo("user1");
        }

        @DisplayName("Should return false when request already exists for same login Id")
        @Test
        void loginInvalidRequestAlreadyExists() {
            when(mockLinkedRequestRepository.countByCcmsUser_LoginIdAndStatusIn(eq("user1"),  anyList()))
                    .thenReturn(0);
            when(mockLinkedRequestRepository.countByOldLoginIdAndStatus(eq("user1"),  eq(Status.OPEN)))
                    .thenReturn(1);

            var actualResult = statusValidator.isValid("user1", mockConstraintValidatorContext);

            assertThat(actualResult).isFalse();
            verify(mockLinkedRequestRepository).countByCcmsUser_LoginIdAndStatusIn(loginIdCaptor.capture(), statusCaptor.capture());
            assertThat(statusCaptor.getValue()).containsExactlyInAnyOrder(Status.OPEN, Status.APPROVED);
            assertThat(loginIdCaptor.getValue()).isEqualTo("user1");
        }

    }
}
