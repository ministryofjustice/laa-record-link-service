package uk.gov.justice.record.link.service;


import mockit.MockUp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.record.link.constants.ValidationConstants;
import uk.gov.justice.record.link.entity.CcmsUser;
import uk.gov.justice.record.link.entity.LinkedRequest;
import uk.gov.justice.record.link.entity.Status;
import uk.gov.justice.record.link.model.UserTransferRequest;
import uk.gov.justice.record.link.respository.CcmsUserRepository;
import uk.gov.justice.record.link.respository.LinkedRequestRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

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

    @Mock
    private CurrentUserService currentUserService;

    @Captor
    private ArgumentCaptor<LinkedRequest> linkedRequestArgumentCaptor;

    @BeforeEach
    void setUp() {
        ccmsUserRepository = mock(CcmsUserRepository.class);
        linkedRequestRepository = mock(LinkedRequestRepository.class);
        currentUserService = mock(CurrentUserService.class);
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
        when(currentUserService.getFirstName()).thenReturn("TestFirstName");
        when(currentUserService.getLastName()).thenReturn("TestLastName");
        when(currentUserService.getUserName()).thenReturn("test-username");
        when(currentUserService.getEmail()).thenReturn("test@example.com");

        UserTransferRequest transferRequest = createUserTransferRequestWithFirmId(
                "loginId", "legacyUserId", "Christopher",
                "James", "Firm 1", "Firm name", "test@example.com");

        CcmsUser ccmsUser = createCcmsUser("Alice", "Alison", "Doe");

        LinkedRequest linkedRequest = new LinkedRequest().toBuilder()
                .additionalInfo("My surname has changed due to marriage.")
                .ccmsUser(ccmsUser)
                .status(Status.OPEN)
                .idamFirstName(currentUserService.getFirstName())
                .idamLastName(currentUserService.getLastName())
                .idamLegacyUserId(currentUserService.getUserName())
                .idamEmail(currentUserService.getEmail())
                .createdDate(LocalDateTime.now())
                .build();

        when(ccmsUserRepository.findByLoginId(transferRequest.getOldLogin())).thenReturn(Optional.of(ccmsUser));

        userTransferService.createRequest(transferRequest);

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

    @Nested
    class AutoApproveRequest {
        @DisplayName("Auto Approved Request: when exact match between CCMS and IDAM")
        @Test
        void approveRequestWhenFirmIdFirstNameAndLastNameMatch() {
            var userTransferRequest = createUserTransferRequestWithFirmId(
                    "loginId", "legacyUserId", "Christopher",
                    "James", "Firm 1", "Firm name", "test@example.com");

            when(ccmsUserRepository.findByLoginId(eq(userTransferRequest.getOldLogin()))).thenReturn(
                    Optional.of(createCcmsUserWithFirmId("Christopher", "James", "Firm 1")));

            userTransferService.createRequest(userTransferRequest);

            verify(linkedRequestRepository).save(linkedRequestArgumentCaptor.capture());

            assertThat(linkedRequestArgumentCaptor.getValue())
                    .extracting("idamLegacyUserId", "idamFirstName", "idamLastName",
                              "idamFirmName", "idamFirmCode",  "idamEmail", "decisionDate",
                            "decisionReason", "laaAssignee", "status")
                    .isEqualTo(Arrays.asList(userTransferRequest.getLegacyUserId(),
                            userTransferRequest.getFirstName(),
                            userTransferRequest.getLastName(),
                            userTransferRequest.getFirmName(),
                            userTransferRequest.getFirmCode(),
                            userTransferRequest.getEmail(),
                            LocalDateTime.now(),
                            ValidationConstants.AUTO_APPROVED_MESSAGE,
                            "System",
                            Status.APPROVED));
        }

        @DisplayName("Auto Approved Request: when initial Match with full names")
        @Test
        void approveRequestWhenInitialMatchWithFullNames() {

            var userTransferRequest = createUserTransferRequestWithFirmId(
                    "loginId", "legacyUserId", "Chris",
                    "James", "Firm 1", "Firm name", "test@example.com");

            when(ccmsUserRepository.findByLoginId(eq(userTransferRequest.getOldLogin()))).thenReturn(
                    Optional.of(createCcmsUserWithFirmId("Christopher", "James", "Firm 1")));

            userTransferService.createRequest(userTransferRequest);

            verify(linkedRequestRepository).save(linkedRequestArgumentCaptor.capture());

            assertThat(linkedRequestArgumentCaptor.getValue())
                    .extracting("idamLegacyUserId", "idamFirstName", "idamLastName",
                            "idamFirmName", "idamFirmCode",  "idamEmail", "decisionDate",
                            "decisionReason", "laaAssignee", "status")
                    .isEqualTo(Arrays.asList(userTransferRequest.getLegacyUserId(),
                            userTransferRequest.getFirstName(),
                            userTransferRequest.getLastName(),
                            userTransferRequest.getFirmName(),
                            userTransferRequest.getFirmCode(),
                            userTransferRequest.getEmail(),
                            LocalDateTime.now(),
                            ValidationConstants.AUTO_APPROVED_MESSAGE,
                            "System",
                            Status.APPROVED));
        }

        @DisplayName("Auto Approved Request: when initial Match with initials")
        @Test
        void approveRequestWhenInitialMatchWithInitials() {
            var userTransferRequest = createUserTransferRequestWithFirmId(
                    "loginId", "legacyUserId", "C",
                    "James", "Firm 1", "Firm name", "test@example.com");

            when(ccmsUserRepository.findByLoginId(eq(userTransferRequest.getOldLogin()))).thenReturn(
                    Optional.of(createCcmsUserWithFirmId("C", "James", "Firm 1")));

            userTransferService.createRequest(userTransferRequest);

            verify(linkedRequestRepository).save(linkedRequestArgumentCaptor.capture());

            assertThat(linkedRequestArgumentCaptor.getValue())
                    .extracting("idamLegacyUserId", "idamFirstName", "idamLastName",
                            "idamFirmName", "idamFirmCode",  "idamEmail", "decisionDate",
                            "decisionReason", "laaAssignee", "status")
                    .isEqualTo(Arrays.asList(userTransferRequest.getLegacyUserId(),
                            userTransferRequest.getFirstName(),
                            userTransferRequest.getLastName(),
                            userTransferRequest.getFirmName(),
                            userTransferRequest.getFirmCode(),
                            userTransferRequest.getEmail(),
                            LocalDateTime.now(),
                            ValidationConstants.AUTO_APPROVED_MESSAGE,
                            "System",
                            Status.APPROVED));
        }

        @DisplayName("Auto Approved Request: when initial Match with combination")
        @Test
        void approveRequestWhenInitialMatchWithCombination() {
            var userTransferRequest = createUserTransferRequestWithFirmId(
                    "loginId", "legacyUserId", "C",
                    "James", "Firm 1", "Firm name", "test@example.com");

            when(ccmsUserRepository.findByLoginId(eq(userTransferRequest.getOldLogin()))).thenReturn(
                    Optional.of(createCcmsUserWithFirmId("Christopher", "James", "Firm 1")));

            userTransferService.createRequest(userTransferRequest);

            verify(linkedRequestRepository).save(linkedRequestArgumentCaptor.capture());

            assertThat(linkedRequestArgumentCaptor.getValue())
                    .extracting("idamLegacyUserId", "idamFirstName", "idamLastName",
                            "idamFirmName", "idamFirmCode",  "idamEmail", "decisionDate",
                            "decisionReason", "laaAssignee", "status")
                    .isEqualTo(Arrays.asList(userTransferRequest.getLegacyUserId(),
                            userTransferRequest.getFirstName(),
                            userTransferRequest.getLastName(),
                            userTransferRequest.getFirmName(),
                            userTransferRequest.getFirmCode(),
                            userTransferRequest.getEmail(),
                            LocalDateTime.now(),
                            ValidationConstants.AUTO_APPROVED_MESSAGE,
                            "System",
                            Status.APPROVED));
        }

        @DisplayName("Auto Approved Request: when Exact match (ignore case)")
        @Test
        void approveRequestWhenExactMatchIgnoreCase() {
            var userTransferRequest = createUserTransferRequestWithFirmId(
                    "loginId", "legacyUserId", "CHRISTOPHER",
                    "JAMES", "FIRM 1", "Firm name", "test@example.com");

            when(ccmsUserRepository.findByLoginId(eq(userTransferRequest.getOldLogin()))).thenReturn(
                    Optional.of(createCcmsUserWithFirmId("Christopher", "James", "Firm 1")));

            userTransferService.createRequest(userTransferRequest);

            verify(linkedRequestRepository).save(linkedRequestArgumentCaptor.capture());

            assertThat(linkedRequestArgumentCaptor.getValue())
                    .extracting("idamLegacyUserId", "idamFirstName", "idamLastName",
                            "idamFirmName", "idamFirmCode",  "idamEmail", "decisionDate",
                            "decisionReason", "laaAssignee", "status")
                    .isEqualTo(Arrays.asList(userTransferRequest.getLegacyUserId(),
                            userTransferRequest.getFirstName(),
                            userTransferRequest.getLastName(),
                            userTransferRequest.getFirmName(),
                            userTransferRequest.getFirmCode(),
                            userTransferRequest.getEmail(),
                            LocalDateTime.now(),
                            ValidationConstants.AUTO_APPROVED_MESSAGE,
                            "System",
                            Status.APPROVED));
        }

        @DisplayName("Not Auto Approved Request: when no match on lastname")
        @Test
        void notAutoApprovedRequestWhenNoMatchOnLastName() {
            var userTransferRequest = createUserTransferRequestWithFirmId("loginId", "legacyUserId", "CHRISTOPHER",
                    "James", "Firm 1", "Firm name", "test@example.com");

            when(ccmsUserRepository.findByLoginId(eq(userTransferRequest.getOldLogin()))).thenReturn(
                    Optional.of(createCcmsUserWithFirmId("Christopher", "John", "Firm 1")));

            userTransferService.createRequest(userTransferRequest);

            verify(linkedRequestRepository).save(linkedRequestArgumentCaptor.capture());

            assertThat(linkedRequestArgumentCaptor.getValue())
                    .extracting("idamLegacyUserId", "idamFirstName", "idamLastName",
                            "idamFirmName", "idamFirmCode",  "idamEmail", "createdDate",
                             "status")
                    .isEqualTo(Arrays.asList(userTransferRequest.getLegacyUserId(),
                            userTransferRequest.getFirstName(),
                            userTransferRequest.getLastName(),
                            userTransferRequest.getFirmName(),
                            userTransferRequest.getFirmCode(),
                            userTransferRequest.getEmail(),
                            LocalDateTime.now(),
                            Status.OPEN));
        }

        @DisplayName("Not Auto Approved Request: when no match on Firm")
        @Test
        void  firmsNotMatch() {
            var userTransferRequest = createUserTransferRequestWithFirmId(
                    "loginId", "legacyUserId", "CHRISTOPHER",
                    "JAMES", "Firm 2", "Firm name", "test@example.com");

            when(ccmsUserRepository.findByLoginId(eq(userTransferRequest.getOldLogin()))).thenReturn(
                    Optional.of(createCcmsUserWithFirmId("Christopher", "James", "Firm 1")));

            userTransferService.createRequest(userTransferRequest);

            verify(linkedRequestRepository).save(linkedRequestArgumentCaptor.capture());

            assertThat(linkedRequestArgumentCaptor.getValue())
                    .extracting("idamLegacyUserId", "idamFirstName", "idamLastName",
                            "idamFirmName", "idamFirmCode",  "idamEmail", "createdDate",
                            "status")
                    .isEqualTo(Arrays.asList(userTransferRequest.getLegacyUserId(),
                            userTransferRequest.getFirstName(),
                            userTransferRequest.getLastName(),
                            userTransferRequest.getFirmName(),
                            userTransferRequest.getFirmCode(),
                            userTransferRequest.getEmail(),
                            LocalDateTime.now(),
                            Status.OPEN));
        }

        @DisplayName("Not Auto Approved Request: when no match on Initial")
        @Test
        void noMatchOnInitial() {
            var userTransferRequest = createUserTransferRequestWithFirmId(
                    "loginId", "legacyUserId", "Christopher",
                    "John", "Firm 1", "Firm name", "test@example.com");

            when(ccmsUserRepository.findByLoginId(eq(userTransferRequest.getOldLogin()))).thenReturn(
                    Optional.of(createCcmsUserWithFirmId("Kris", "John", "Firm 1")));

            userTransferService.createRequest(userTransferRequest);

            verify(linkedRequestRepository).save(linkedRequestArgumentCaptor.capture());

            assertThat(linkedRequestArgumentCaptor.getValue())
                    .extracting("idamLegacyUserId", "idamFirstName", "idamLastName",
                            "idamFirmName", "idamFirmCode",  "idamEmail", "createdDate",
                            "status")
                    .isEqualTo(Arrays.asList(userTransferRequest.getLegacyUserId(),
                            userTransferRequest.getFirstName(),
                            userTransferRequest.getLastName(),
                            userTransferRequest.getFirmName(),
                            userTransferRequest.getFirmCode(),
                            userTransferRequest.getEmail(),
                            LocalDateTime.now(),
                            Status.OPEN));
        }

        @DisplayName("Not Auto Approved Request: when special character missing")
        @Test
        void specialCharacterMissing() {
            var userTransferRequest = createUserTransferRequestWithFirmId(
                    "loginId", "legacyUserId", "Kris",
                    "O’Brien", "Firm 1", "Firm name", "test@example.com");

            when(ccmsUserRepository.findByLoginId(eq(userTransferRequest.getOldLogin()))).thenReturn(
                    Optional.of(createCcmsUserWithFirmId("Kris", "OBrien", "Firm 1")));

            userTransferService.createRequest(userTransferRequest);

            verify(linkedRequestRepository).save(linkedRequestArgumentCaptor.capture());

            assertThat(linkedRequestArgumentCaptor.getValue())
                    .extracting("idamLegacyUserId", "idamFirstName", "idamLastName",
                            "idamFirmName", "idamFirmCode",  "idamEmail", "createdDate",
                            "status")
                    .isEqualTo(Arrays.asList(userTransferRequest.getLegacyUserId(),
                            userTransferRequest.getFirstName(),
                            userTransferRequest.getLastName(),
                            userTransferRequest.getFirmName(),
                            userTransferRequest.getFirmCode(),
                            userTransferRequest.getEmail(),
                            LocalDateTime.now(),
                            Status.OPEN));

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

    private CcmsUser createCcmsUserWithFirmId(final String firstName, final String lastName, final String firmId) {
        return CcmsUser.builder()
                .firmCode(firmId)
                .firstName(firstName)
                .lastName(lastName)
                .build();
    }

    private UserTransferRequest createUserTransferRequestWithFirmId(final String oldLogin, final String legacyUserId,
                                                                     final String firstName, final String lastName,
                                                                    final String firmId, final String firmName, final String email) {
        UserTransferRequest transferRequest = new UserTransferRequest();
        transferRequest.setOldLogin(oldLogin);
        transferRequest.setLegacyUserId(legacyUserId);
        transferRequest.setFirstName(firstName);
        transferRequest.setLastName(lastName);
        transferRequest.setFirmCode(firmId);
        transferRequest.setFirmName(firmName);
        transferRequest.setEmail(email);
        return transferRequest;
    }
}
