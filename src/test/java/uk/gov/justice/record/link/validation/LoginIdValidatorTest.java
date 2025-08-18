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
import uk.gov.justice.record.link.entity.CcmsUser;
import uk.gov.justice.record.link.respository.CcmsUserRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LoginIdValidatorTest {
    @Mock
    private CcmsUserRepository mockCcmsUserRepository;
    @Mock
    private ConstraintValidatorContext mockConstraintValidatorContext;
    @InjectMocks
    private LoginIdValidator loginIdValidator;
    @Captor
    private ArgumentCaptor<String> loginIdCaptor;

    @Nested
    class IsValid {
        @DisplayName("Should return true if login id is present in CCMS_USER table")
        @Test
        void validLoginId() {
            when(mockCcmsUserRepository.findByLoginId(eq("loginId"))).thenReturn(Optional.of(createCcmsUser()));

            var actualResult = loginIdValidator.isValid("loginId", mockConstraintValidatorContext);

            assertThat(actualResult).isTrue();

            verify(mockCcmsUserRepository).findByLoginId(loginIdCaptor.capture());
            assertThat(loginIdCaptor.getValue()).isEqualTo("loginId");

        }

        @DisplayName("Should return false if login id is not present in CCMS_USER table")
        @Test
        void invalidLoginId() {
            when(mockCcmsUserRepository.findByLoginId(eq("loginId"))).thenReturn(Optional.empty());

            var actualResult = loginIdValidator.isValid("loginId", mockConstraintValidatorContext);

            assertThat(actualResult).isFalse();
        }
    }


    private CcmsUser createCcmsUser() {
        return CcmsUser.builder()
                .loginId("loginId")
                .firstName("firstName")
                .lastName("lastName")
                .email("<EMAIL>")
                .firmCode("firmCode")
                .build();
    }
}
