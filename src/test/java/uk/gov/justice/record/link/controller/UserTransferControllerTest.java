package uk.gov.justice.record.link.controller;

import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.thymeleaf.util.StringUtils;
import uk.gov.justice.record.link.config.DevSecurityConfig;
import uk.gov.justice.record.link.constants.SilasConstants;
import uk.gov.justice.record.link.constants.ValidationConstants;
import uk.gov.justice.record.link.entity.CcmsUser;
import uk.gov.justice.record.link.entity.LinkedRequest;
import uk.gov.justice.record.link.entity.Status;
import uk.gov.justice.record.link.model.UserTransferRequest;
import uk.gov.justice.record.link.respository.CcmsUserRepository;
import uk.gov.justice.record.link.respository.LinkedRequestRepository;
import uk.gov.justice.record.link.service.CurrentUserService;
import uk.gov.justice.record.link.service.OidcTokenClaimsExtractor;
import uk.gov.justice.record.link.service.UserTransferService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ActiveProfiles("local")
@WebMvcTest(UserTransferController.class)
@Import(DevSecurityConfig.class)
public class UserTransferControllerTest {

    private static Validator validator;
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private UserTransferService userTransferService;
    @MockitoBean
    private LinkedRequestRepository mockLinkedRequestRepository;
    @MockitoBean
    private CcmsUserRepository mockCcmsUserRepository;
    @MockitoBean
    private CurrentUserService currentUserService;
    @Captor
    private ArgumentCaptor<UserTransferRequest> userTransferRequestCaptor;
    @Captor
    private ArgumentCaptor<String> reasonCaptor;

    @Test
    void shouldRenderHomePage() throws Exception {
        OidcTokenClaimsExtractor mockClaims = mock(OidcTokenClaimsExtractor.class);
        when(mockClaims.getUserName()).thenReturn("Alice");
        when(currentUserService.getCurrentUserClaims()).thenReturn(mockClaims);

        Page<LinkedRequest> mockPage = new PageImpl<>(List.of());
        when(userTransferService.getRequestsForCurrentUser(anyString(), any(Pageable.class)))
                .thenReturn(mockPage);
        mockMvc.perform(get("/external/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Nested
    class CheckAnswer {
        @DisplayName("Should render preview with user data")
        @Test
        void shouldRenderPreviewPageWithUserData() throws Exception {
            MvcResult result = mockMvc.perform(post("/external/check-answers")
                            .with(oidcLogin()
                                    .idToken(token -> token.claim(SilasConstants.FIRM_CODE, "1234")))
                            .param("oldLogin", "Alice")
                            .param("additionalInfo", "My surname has changed due to marriage."))
                    .andExpect(status().isOk())
                    .andExpect(view().name("check-answers"))
                    .andExpect(model().attributeExists("userTransferRequest"))
                    .andReturn();
            String html = result.getResponse().getContentAsString();

            assertTrue(html.contains("Alice"));
            assertTrue(html.contains("My surname has changed due to marriage."));
        }

        @DisplayName("User transfer form with empty CCMS login id")
        @Test
        void givenEmptyUsername_shouldTriggerPatternViolation() throws Exception {
            MvcResult result = mockMvc.perform(post("/external/check-answers")
                            .param("additionalInfo", "My surname has changed due to marriage."))
                    .andExpect(status().isOk())
                    .andExpect(view().name("user-transfer-request"))
                    .andExpect(model().hasErrors())
                    .andExpect(model().attributeHasFieldErrors("userTransferRequest", "oldLogin"))
                    .andReturn();

            String html = result.getResponse().getContentAsString();
            assertTrue(html.contains("Enter CCMS username"));
        }

        @DisplayName("Should not trigger any other validation from check answer page")
        @Test
        void shouldNotTriggerAnyOtherValidationFromCheckAnswerPage() throws Exception {
            mockMvc.perform(post("/external/check-answers")
                            .with(oidcLogin()
                                    .idToken(token -> token.claim(SilasConstants.FIRM_CODE, "1234")))
                            .param("oldLogin", "invalidLoginId")
                            .param("additionalInfo", "My surname has changed due to marriage."))
                    .andExpect(status().isOk())
                    .andReturn();

            verify(mockLinkedRequestRepository, times(0)).countByCcmsUser_LoginIdAndStatusIn(anyString(), anyList());
            verify(mockCcmsUserRepository, times(0)).findByLoginId(anyString());
        }

        @DisplayName("Should add firm id claim to user transfer request")
        @Test
        void addsClaimsToRequest() throws Exception {
            var result = mockMvc.perform(post("/external/check-answers")
                            .with(oidcLogin()
                                    .idToken(token -> token.claim(SilasConstants.FIRM_CODE, "1234")))
                            .param("oldLogin", "Alice")
                            .param("additionalInfo", "My surname has changed due to marriage."))
                    .andExpect(model().attribute("userTransferRequest",
                            hasProperty("firmCode", is("1234"))
                    ))
                    .andExpect(status().isOk())
                    .andReturn();
            String html = result.getResponse().getContentAsString();
            assertTrue(html.contains("firmCode"));
        }


    }

    @Nested
    class LinkUserRequest {
        @DisplayName("Render user transfer form")
        @Test
        void shouldRenderUserTransferForm() throws Exception {
            mockMvc.perform(get("/external/user-transfer-request")
                            .param("oldLogin", "Alice")
                            .param("additionalInfo", "My surname has changed due to marriage."))
                    .andExpect(status().isOk())
                    .andExpect(view().name("user-transfer-request"))
                    .andExpect(model().attributeExists("userTransferRequest"))
                    .andReturn();
        }

        @DisplayName("Complete user transfer form")
        @Test
        void shouldCompleteUserTransferRequest() throws Exception {

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

            when(mockCcmsUserRepository.findByLoginId(anyString())).thenReturn(Optional.of(ccmsUser));
            when(mockLinkedRequestRepository.countByCcmsUser_LoginIdAndStatusIn(anyString(), anyList())).thenReturn(0);
            when(mockCcmsUserRepository.existsByFirmCode(anyString())).thenReturn(true);

            mockMvc.perform(post("/external/request-confirmation")
                            .param("oldLogin", "Alice")
                            .param("additionalInfo", "My surname has changed due to marriage.")
                            .param("firmCode", "1234"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("request-created"))
                    .andExpect(model().attributeExists("userTransferRequest"))
                    .andReturn();

            verify(userTransferService, times(1)).save(any(UserTransferRequest.class));
        }

        @DisplayName("Should return request rejected for login id in OPEN or APPROVED status")
        @Test
        void shouldReturnRequestRejectedForLoginIdInOpenOrApprovedStatus() throws Exception {
            when(mockLinkedRequestRepository.countByCcmsUser_LoginIdAndStatusIn(anyString(), anyList())).thenReturn(1);
            when(mockCcmsUserRepository.findByLoginId(anyString())).thenReturn(Optional.of(ccmsUser));
            when(mockCcmsUserRepository.existsByFirmCode(anyString())).thenReturn(true);
            doNothing().when(userTransferService).rejectRequest(userTransferRequestCaptor.capture(), reasonCaptor.capture());

            mockMvc.perform(post("/external/request-confirmation")
                            .param("oldLogin", "Alice")
                            .param("additionalInfo", "My surname has changed due to marriage.")
                            .param("firmCode", "1234"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("request_rejected"))
                    .andReturn();


            assertThat(reasonCaptor.getValue()).isEqualTo("Login processed");
            assertThat(userTransferRequestCaptor.getValue()).extracting("oldLogin", "additionalInfo")
                    .isEqualTo(Arrays.asList("Alice", "My surname has changed due to marriage."));

            verify(userTransferService, times(0)).save(any(UserTransferRequest.class));
        }

        @DisplayName("Should return request accepted when login id not OPEN or APPROVED status")
        @Test
        void shouldReturnSuccessForLoginIdNotInOpenOrApprovedStatus() throws Exception {
            when(mockLinkedRequestRepository.countByCcmsUser_LoginIdAndStatusIn(anyString(), anyList())).thenReturn(0);
            when(mockCcmsUserRepository.findByLoginId(anyString())).thenReturn(Optional.of(ccmsUser));
            when(mockCcmsUserRepository.existsByFirmCode(anyString())).thenReturn(true);
            doNothing().when(userTransferService).rejectRequest(any(UserTransferRequest.class), anyString());

            mockMvc.perform(post("/external/request-confirmation")
                            .param("oldLogin", "Alice")
                            .param("additionalInfo", "My surname has changed due to marriage.")
                            .param("firmCode", "1234"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("request-created"))
                    .andReturn();

            verify(userTransferService, times(0)).rejectRequest(any(UserTransferRequest.class), anyString());

            verify(userTransferService, times(1)).save(userTransferRequestCaptor.capture());

            assertThat(userTransferRequestCaptor.getValue()).extracting("oldLogin", "additionalInfo")
                    .isEqualTo(Arrays.asList("Alice", "My surname has changed due to marriage."));
        }

        @DisplayName("Should return request rejected when login id does not exist in CCMS_USER")
        @Test
        void shouldReturnRequestRejectedWhenLoginIdIsNotValid() throws Exception {
            when(mockLinkedRequestRepository.countByCcmsUser_LoginIdAndStatusIn(anyString(), anyList())).thenReturn(0);
            when(mockCcmsUserRepository.findByLoginId(anyString())).thenReturn(Optional.empty());
            doNothing().when(userTransferService).rejectRequest(userTransferRequestCaptor.capture(), reasonCaptor.capture());

            mockMvc.perform(post("/external/request-confirmation")
                            .param("oldLogin", "invalidLoginId")
                            .param("additionalInfo", "My surname has changed due to marriage."))
                    .andExpect(status().isOk())
                    .andExpect(view().name("request_rejected"))
                    .andReturn();

            assertThat(reasonCaptor.getValue()).isEqualTo("No match found");
            assertThat(userTransferRequestCaptor.getValue()).extracting("oldLogin", "additionalInfo")
                    .isEqualTo(Arrays.asList("invalidLoginId", "My surname has changed due to marriage."));

            verify(userTransferService, times(0)).save(any(UserTransferRequest.class));
        }

        @DisplayName("Should return request accepted when login id exist in CCMS_USER")
        @Test
        void shouldReturnRequestAcceptedWhenLoginIdIsValid() throws Exception {
            when(mockLinkedRequestRepository.countByCcmsUser_LoginIdAndStatusIn(anyString(), anyList())).thenReturn(0);
            when(mockCcmsUserRepository.findByLoginId(anyString())).thenReturn(Optional.of(ccmsUser));
            when(mockCcmsUserRepository.existsByFirmCode(anyString())).thenReturn(true);
            doNothing().when(userTransferService).rejectRequest(any(UserTransferRequest.class), anyString());

            mockMvc.perform(post("/external/request-confirmation")
                            .param("oldLogin", "Alice")
                            .param("additionalInfo", "My surname has changed due to marriage.")
                            .param("firmCode", "1234"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("request-created"))
                    .andReturn();

            verify(userTransferService, times(0)).rejectRequest(any(UserTransferRequest.class), anyString());

            verify(userTransferService, times(1)).save(userTransferRequestCaptor.capture());

            assertThat(userTransferRequestCaptor.getValue()).extracting("oldLogin", "additionalInfo")
                    .isEqualTo(Arrays.asList("Alice", "My surname has changed due to marriage."));
        }

        @DisplayName("Order of validation: Login id validation should take priority when both login id and status are invalid")
        @Test
        void shouldReturnRequestRejectedWhenLoginIdAndStatusAreInvalid() throws Exception {
            when(mockLinkedRequestRepository.countByCcmsUser_LoginIdAndStatusIn(anyString(), anyList())).thenReturn(1);
            when(mockCcmsUserRepository.findByLoginId(anyString())).thenReturn(Optional.empty());
            doNothing().when(userTransferService).rejectRequest(userTransferRequestCaptor.capture(), reasonCaptor.capture());

            mockMvc.perform(post("/external/request-confirmation")
                            .param("oldLogin", "invalidLoginId")
                            .param("additionalInfo", "My surname has changed due to marriage."))
                    .andExpect(status().isOk())
                    .andExpect(view().name("request_rejected"))
                    .andReturn();

            assertThat(reasonCaptor.getValue()).isEqualTo("No match found");
        }

        @DisplayName("Order of validation: Should validate status only after login iD is valid")
        @Test
        void shouldValidateStatusOnlyAfterLoginIdIsValid() throws Exception {
            when(mockLinkedRequestRepository.countByCcmsUser_LoginIdAndStatusIn(anyString(), anyList())).thenReturn(1);
            when(mockCcmsUserRepository.findByLoginId(anyString())).thenReturn(Optional.of(ccmsUser));
            when(mockCcmsUserRepository.existsByFirmCode(anyString())).thenReturn(true);
            doNothing().when(userTransferService).rejectRequest(userTransferRequestCaptor.capture(), reasonCaptor.capture());

            mockMvc.perform(post("/external/request-confirmation")
                            .param("oldLogin", "invalidLoginId")
                            .param("additionalInfo", "My surname has changed due to marriage.")
                            .param("firmCode", "1234"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("request_rejected"))
                    .andReturn();

            assertThat(reasonCaptor.getValue()).isEqualTo("Login processed");
        }

        @DisplayName("Order of validation when login id is valid, account is not closed, and status is not opened")
        @Test
        void shouldReturnRequestCreatedWhenLoginIdIsValidAndAccountIsNotClosed() throws Exception {
            when(mockCcmsUserRepository.findByLoginId(anyString())).thenReturn(Optional.of(ccmsUser));
            when(mockLinkedRequestRepository.countByCcmsUser_LoginIdAndStatusIn(anyString(), anyList())).thenReturn(0);
            when(mockCcmsUserRepository.existsByFirmCode(anyString())).thenReturn(true);
            doNothing().when(userTransferService).rejectRequest(userTransferRequestCaptor.capture(), reasonCaptor.capture());

            mockMvc.perform(post("/external/request-confirmation")
                            .param("oldLogin", "Alice")
                            .param("additionalInfo", "My surname has changed due to marriage.")
                            .param("firmCode", "1234"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("request-created"))
                    .andReturn();

            verify(mockCcmsUserRepository, times(2)).findByLoginId(anyString());
            verify(userTransferService, times(0)).rejectRequest(any(UserTransferRequest.class), anyString());
        }

        @DisplayName("Should validate request when login id is valid and email of a LAA closed account")
        @Test
        void shouldReturnRequestRejectedWhenEmailIsOfLaaClosedAccount() throws Exception {
            when(mockCcmsUserRepository.findByLoginId(anyString())).thenReturn(Optional.of(user2));
            doNothing().when(userTransferService).rejectRequest(userTransferRequestCaptor.capture(), reasonCaptor.capture());

            mockMvc.perform(post("/external/request-confirmation")
                            .param("oldLogin", "invalidLoginId")
                            .param("additionalInfo", "My surname has changed due to marriage."))
                    .andExpect(status().isOk())
                    .andExpect(view().name("request_rejected"))
                    .andReturn();

            assertThat(reasonCaptor.getValue()).isEqualTo("Account closed");
            verify(mockCcmsUserRepository, times(2)).findByLoginId("invalidLoginId");
        }

        @DisplayName("Login id validation should take priority when login id invalid and user account is closed")
        @Test
        void shouldReturnRequestRejectedWhenLoginIdIsInvalidAndAccountIsClosed() throws Exception {
            when(mockCcmsUserRepository.findByLoginId("Mo")).thenReturn(Optional.empty());
            doNothing().when(userTransferService).rejectRequest(userTransferRequestCaptor.capture(), reasonCaptor.capture());

            mockMvc.perform(post("/external/request-confirmation")
                            .param("oldLogin", "Mo")
                            .param("additionalInfo", "My surname has changed due to marriage."))
                    .andExpect(status().isOk())
                    .andExpect(view().name("request_rejected"))
                    .andReturn();

            verify(mockCcmsUserRepository, times(1)).findByLoginId("Mo");
            assertThat(reasonCaptor.getValue()).isEqualTo("No match found");
        }

        @DisplayName("Should return success if IDAM firm code matches CCMS firm code")
        @Test
        void shouldRejectLinkRequestIfIdamFirmCodeDoesNotMatchCcmsFirmCode() throws Exception {
            when(mockLinkedRequestRepository.countByCcmsUser_LoginIdAndStatusIn(anyString(), anyList())).thenReturn(0);
            when(mockCcmsUserRepository.findByLoginId(anyString())).thenReturn(Optional.of(ccmsUser));
            when(mockCcmsUserRepository.existsByFirmCode(anyString())).thenReturn(true);

            mockMvc.perform(post("/external/request-confirmation")
                            .param("oldLogin", "validLoginId")
                            .param("additionalInfo", "My surname has changed due to marriage.")
                            .param("firmCode", "1234"))
                    .andExpect(status().isOk())
                    .andExpect(model().hasNoErrors())
                    .andExpect(view().name("request-created"));

            verify(userTransferService, times(0)).rejectRequest(any(UserTransferRequest.class), anyString());

            verify(userTransferService, times(1)).save(userTransferRequestCaptor.capture());
            assertThat(userTransferRequestCaptor.getValue()).extracting("oldLogin", "additionalInfo", "firmCode")
                    .isEqualTo(Arrays.asList("validLoginId", "My surname has changed due to marriage.", "1234"));

        }

        @DisplayName("Should return request rejected when IDAM firm code does not match CCMS firm code")
        @Test
        void shouldReturnRequestRejectedWhenIdamFirmCodeDoesNotMatchCcmsFirmCode() throws Exception {
            when(mockLinkedRequestRepository.countByCcmsUser_LoginIdAndStatusIn(anyString(), anyList())).thenReturn(0);
            when(mockCcmsUserRepository.findByLoginId(anyString())).thenReturn(Optional.of(ccmsUser));
            when(mockCcmsUserRepository.existsByFirmCode(anyString())).thenReturn(false);
            doNothing().when(userTransferService).rejectRequest(userTransferRequestCaptor.capture(), reasonCaptor.capture());

            mockMvc.perform(post("/external/request-confirmation")
                            .param("oldLogin", "validLoginId")
                            .param("additionalInfo", "My surname has changed due to marriage.")
                            .param("firmCode", "invalidCode"))
                    .andExpect(status().isOk())
                    .andExpect(model().hasErrors())
                    .andExpect(model().attributeHasFieldErrors("userTransferRequest", "firmCode"))
                    .andExpect(view().name("request_rejected"));

            verify(userTransferService, times(1)).rejectRequest(any(UserTransferRequest.class), anyString());

            verify(userTransferService, times(0)).save(userTransferRequestCaptor.capture());

            assertThat(reasonCaptor.getValue()).isEqualTo(ValidationConstants.INVALID_FIRM_ID_MESSAGE);

        }

        @DisplayName("Order of validation: firm code should take priority over status when both are invalid")
        @Test
        void firmCodeTakestPriorityOverStatus() throws Exception {
            when(mockLinkedRequestRepository.countByCcmsUser_LoginIdAndStatusIn(anyString(), anyList())).thenReturn(1);
            when(mockCcmsUserRepository.findByLoginId(anyString())).thenReturn(Optional.of(ccmsUser));
            when(mockCcmsUserRepository.existsByFirmCode(anyString())).thenReturn(false);
            doNothing().when(userTransferService).rejectRequest(userTransferRequestCaptor.capture(), reasonCaptor.capture());

            mockMvc.perform(post("/external/request-confirmation")
                            .param("oldLogin", "validLoginId")
                            .param("additionalInfo", "My surname has changed due to marriage.")
                            .param("firmCode", "invalidCode"))
                    .andExpect(status().isOk())
                    .andExpect(model().attributeHasFieldErrors("userTransferRequest", "firmCode"))
                    .andExpect(view().name("request_rejected"));

            verify(userTransferService, times(1)).rejectRequest(any(UserTransferRequest.class), anyString());

            verify(userTransferService, times(0)).save(userTransferRequestCaptor.capture());

            assertThat(reasonCaptor.getValue()).isEqualTo(ValidationConstants.INVALID_FIRM_ID_MESSAGE);

        }

        @DisplayName("Order of validation: Login id should take priority when both status, login id and firm code are invalid")
        @Test
        void loginIdTakestPriorityOverStatusAndFirmCode() throws Exception {
            when(mockLinkedRequestRepository.countByCcmsUser_LoginIdAndStatusIn(anyString(), anyList())).thenReturn(1);
            when(mockCcmsUserRepository.findByLoginId(anyString())).thenReturn(Optional.empty());
            when(mockCcmsUserRepository.existsByFirmCode(anyString())).thenReturn(false);
            doNothing().when(userTransferService).rejectRequest(userTransferRequestCaptor.capture(), reasonCaptor.capture());

            mockMvc.perform(post("/external/request-confirmation")
                            .param("oldLogin", "invalidLoginId")
                            .param("additionalInfo", "My surname has changed due to marriage.")
                            .param("firmCode", "invalidCode"))
                    .andExpect(status().isOk())
                    .andExpect(model().attributeHasFieldErrors("userTransferRequest", "oldLogin"))
                    .andExpect(view().name("request_rejected"));

            verify(userTransferService, times(1)).rejectRequest(any(UserTransferRequest.class), anyString());

            verify(userTransferService, times(0)).save(userTransferRequestCaptor.capture());

            assertThat(reasonCaptor.getValue()).isEqualTo(ValidationConstants.INVALID_LOGIN_ID_MESSAGE);
        }
    }

    private final CcmsUser ccmsUser = CcmsUser.builder()
            .loginId("Alice")
            .firstName("Alison")
            .lastName("Doe")
            .email("test@test.com")
            .build();

    private final CcmsUser user2 = CcmsUser.builder()
            .loginId("Alice")
            .firstName("Alison")
            .lastName("Doe")
            .email("online-support@legalaid.gsi.gov.uk")
            .build();

}