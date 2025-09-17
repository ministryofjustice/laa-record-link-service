package uk.gov.justice.record.link.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.record.link.entity.Status;
import uk.gov.justice.record.link.model.UserTransferRequest;
import uk.gov.justice.record.link.respository.LinkedRequestRepository;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserTransferRequestValidatorTest {

    @Mock
    private LinkedRequestRepository mockLinkedRequestRepository;
    @Mock
    private ConstraintValidatorContext mockConstraintValidatorContext;
    @InjectMocks
    private UserTransferValidator userTransferValidator;

    @Nested
    class IsValid {
        @DisplayName("Should return true on creating new valid linked request")
        @Test
        void validRequest() {
            when(mockLinkedRequestRepository.countByOldLoginIdAndIdamFirmCodeAndStatusIn(eq("user2"), eq("234"), eq(List.of(Status.APPROVED)))).thenReturn(0);

            UserTransferRequest userTransferRequest = new UserTransferRequest();
            userTransferRequest.setFirmCode("234");
            userTransferRequest.setOldLogin("user2");
            var actualResult = userTransferValidator.isValid(userTransferRequest, mockConstraintValidatorContext);
            assertThat(actualResult).isTrue();
        }

        @DisplayName("Should return false if associated approved linked request present")
        @Test
        void invalidRequest() {
            when(mockLinkedRequestRepository.countByOldLoginIdAndIdamFirmCodeAndStatusIn(eq("user2"), eq("234"), eq(List.of(Status.APPROVED)))).thenReturn(1);

            UserTransferRequest userTransferRequest = new UserTransferRequest();
            userTransferRequest.setFirmCode("234");
            userTransferRequest.setOldLogin("user2");
            var actualResult = userTransferValidator.isValid(userTransferRequest, mockConstraintValidatorContext);
            assertThat(actualResult).isFalse();
        }

        @DisplayName("Should return true if oldLoginId not present to allow login id validator to do checks")
        @Test
        void invalidLoginId() {
            UserTransferRequest userTransferRequest = new UserTransferRequest();
            userTransferRequest.setFirmCode("234");
            userTransferRequest.setOldLogin(null);
            var actualResult = userTransferValidator.isValid(userTransferRequest, mockConstraintValidatorContext);
            assertThat(actualResult).isTrue();
        }
    }
}
