package uk.gov.justice.record.link.service;

import mockit.MockUp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.util.StringUtils;
import uk.gov.justice.record.link.entity.CcmsUser;
import uk.gov.justice.record.link.entity.LinkedRequest;
import uk.gov.justice.record.link.entity.Status;
import uk.gov.justice.record.link.model.UserTransferRequest;
import uk.gov.justice.record.link.respository.CcmsUserRepository;
import uk.gov.justice.record.link.respository.LinkedRequestRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserTransferServiceTest {

    private UserTransferService userTransferService;
    private CcmsUserRepository ccmsUserRepository;
    private LinkedRequestRepository linkedRequestRepository;
    @Captor
    private ArgumentCaptor<LinkedRequest> linkedRequestArgumentCaptor;

    @BeforeEach
    void setUp() {
        ccmsUserRepository = mock(CcmsUserRepository.class);
        linkedRequestRepository = mock(LinkedRequestRepository.class);
        userTransferService = new UserTransferService(linkedRequestRepository, ccmsUserRepository);
        new MockUp<LocalDateTime>() {
            @mockit.Mock
            public LocalDateTime now() {
                return LocalDateTime.of(1, 1, 1, 1, 1);
            }
        };
    }

    @Test
    void saveUserTransferRequest() {

        UserTransferRequest transferRequest = createUserTransferRequest("Alice", "My surname has changed due to marriage.");
        CcmsUser ccmsUser = createCcmsUser("Alice", "Alison", "Doe");

        LinkedRequest linkedRequest = new LinkedRequest().toBuilder()
                .additionalInfo("My surname has changed due to marriage.")
                .ccmsUser(ccmsUser)
                .status(Status.OPEN)
                .idamFirstName("TODO in STB-2368")
                .idamLastName("TODO in STB-2368")
                .idamLegacyUserId(UUID.randomUUID().toString())
                .idamEmail(StringUtils.randomAlphanumeric(6))
                .createdDate(LocalDateTime.now())
                .build();

        when(ccmsUserRepository.findByLoginId(transferRequest.getOldLogin())).thenReturn(Optional.of(ccmsUser));

        userTransferService.save(transferRequest);

        verify(ccmsUserRepository).findByLoginId(transferRequest.getOldLogin());
        verify(ccmsUserRepository, times(1)).findByLoginId(transferRequest.getOldLogin());
    }
    
    @Nested 
    class RejectRequest {
        
        @DisplayName("Should save request when login id found in CCMS with valid CCMS ID")
        @Test
        void shouldRejectRequestWhenLoginIdFoundInCcms() {
            var userTransferRequest = createUserTransferRequest("Bob", "My surname has changed due to marriage.");
            var ccmsUser = createCcmsUser("123", "Alison", "Doe");
            when(ccmsUserRepository.findByLoginId(eq(userTransferRequest.getOldLogin()))).thenReturn(
                    Optional.of(ccmsUser));


            userTransferService.rejectRequest(userTransferRequest, "No match found");

            verify(linkedRequestRepository).save(linkedRequestArgumentCaptor.capture());


            assertThat(linkedRequestArgumentCaptor.getValue())
                        .extracting("ccmsUser", "oldLoginId", "additionalInfo", "status", "decisionDate", "decisionReason", "laaAssignee")
                        .isEqualTo(Arrays.asList(ccmsUser,
                                userTransferRequest.getOldLogin(),
                                userTransferRequest.getAdditionalInfo(),
                                Status.REJECTED,
                                LocalDateTime.now(),
                                "No match found",
                                "System"));
        }

        @DisplayName("Should save request when login id not found in CCMS with null CCMS user")
        @Test
        void shouldRejectRequestWhenLoginIdNotFoundInCcms() {
            var userTransferRequest = createUserTransferRequest("Bob", "My surname has changed due to marriage.");
            when(ccmsUserRepository.findByLoginId(eq(userTransferRequest.getOldLogin()))).thenReturn(
                    Optional.empty());

            userTransferService.rejectRequest(userTransferRequest, "No match found");

            verify(linkedRequestRepository).save(linkedRequestArgumentCaptor.capture());

            assertThat(linkedRequestArgumentCaptor.getValue())
                    .extracting("ccmsUser", "oldLoginId", "additionalInfo", "status", "decisionDate", "decisionReason", "laaAssignee")
                    .isEqualTo(Arrays.asList(null,
                            userTransferRequest.getOldLogin(),
                            userTransferRequest.getAdditionalInfo(),
                            Status.REJECTED,
                            LocalDateTime.now(),
                            "No match found",
                            "System"));
        }
    }


    private  UserTransferRequest createUserTransferRequest(final String oldLogin, final String additionalInfo) {
        UserTransferRequest transferRequest = new UserTransferRequest();
        transferRequest.setOldLogin(oldLogin);
        transferRequest.setAdditionalInfo(additionalInfo);
        return transferRequest;
    }

    private CcmsUser createCcmsUser(final String loginId, final String firstName, final String lastName) {
        return CcmsUser.builder()
                .loginId(loginId)
                .firstName(firstName)
                .lastName(lastName)
                .build();
    }
}
