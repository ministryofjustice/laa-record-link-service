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
import uk.gov.justice.record.link.respository.CcmsUserRepository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FirmCodeValidatorTest {
    private static final String VALID_FIRM_CODE = "valid_firm_id";
    private static final String INVALID_FIRM_CODE = "invalid_firm_id";

    @Mock
    private CcmsUserRepository mockCcmsUserRepository;
    @Mock
    private ConstraintValidatorContext mockConstraintValidatorContext;
    @InjectMocks
    private FirmCodeValidator firmIdValidator;

    @Captor
    private ArgumentCaptor<String> firmIdCaptor;


    @Nested
    class IsValid {
        @DisplayName("Should return true if firm id is present in CCMS_USER table")
        @Test
        void validFirmId() {
            when(mockCcmsUserRepository.existsByFirmCode(VALID_FIRM_CODE)).thenReturn(true);

            var actualResult = firmIdValidator.isValid(VALID_FIRM_CODE, mockConstraintValidatorContext);

            verify(mockCcmsUserRepository).existsByFirmCode(firmIdCaptor.capture());

            assertThat(actualResult).isTrue();
            assertThat(firmIdCaptor.getValue()).isEqualTo(VALID_FIRM_CODE);

        }

        @DisplayName("Should return false if firm id is not present in CCMS_USER table")
        @Test
        void invalidFirmId() {
            when(mockCcmsUserRepository.existsByFirmCode(INVALID_FIRM_CODE)).thenReturn(false);

            var actualResult = firmIdValidator.isValid(INVALID_FIRM_CODE, mockConstraintValidatorContext);

            verify(mockCcmsUserRepository).existsByFirmCode(firmIdCaptor.capture());

            assertThat(actualResult).isFalse();
            assertThat(firmIdCaptor.getValue()).isEqualTo(INVALID_FIRM_CODE);
        }

    }
}
