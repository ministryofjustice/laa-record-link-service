package uk.gov.justice.record.link.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.record.link.entity.CcmsUser;
import uk.gov.justice.record.link.respository.CcmsUserRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ClosedAccountValidatorTest {
    @Mock
    private CcmsUserRepository ccmsRepository;
    @Mock
    private ConstraintValidatorContext mockConstraintValidatorContext;
    @InjectMocks
    private ClosedAccountValidator closedAccountValidator;

    private CcmsUser userWithClosedAccount() {
        return CcmsUser.builder()
                .loginId("loginId")
                .firstName("firstName")
                .lastName("lastName")
                .email("online-support@legalaid.gsi.gov.uk")
                .firmCode("firmCode")
                .build();
    }

    private CcmsUser user() {
        return CcmsUser.builder()
                .loginId("user1")
                .firstName("firstName")
                .lastName("lastName")
                .email("fname@test.com")
                .firmCode("firmCode")
                .build();
    }

    @Nested
    class IsValid {

        @DisplayName("Should return false if the email on the CCMS account is an LAA email")
        @Test
        void loginInvalid() {
            when(ccmsRepository.findByLoginId("loginId")).thenReturn(Optional.of(userWithClosedAccount()));

            var actualResult = closedAccountValidator.isValid("loginId", mockConstraintValidatorContext);

            assertThat(actualResult).isFalse();
        }

        @DisplayName("Should return true if the email on the CCMS account is not an LAA email")
        @Test
        void validLoginId() {
            when(ccmsRepository.findByLoginId("user1")).thenReturn(Optional.of(user()));

            var actualResult = closedAccountValidator.isValid("user1", mockConstraintValidatorContext);

            assertThat(actualResult).isTrue();
        }
    }
}