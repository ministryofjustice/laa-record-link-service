package uk.gov.justice.record.link.controller;

import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.thymeleaf.util.StringUtils;
import uk.gov.justice.record.link.entity.CcmsUser;
import uk.gov.justice.record.link.entity.LinkedRequest;
import uk.gov.justice.record.link.entity.Status;
import uk.gov.justice.record.link.model.UserTransferRequest;
import uk.gov.justice.record.link.respository.LinkedRequestRepository;
import uk.gov.justice.record.link.service.UserTransferService;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(UserTransferController.class)
public class UserTransferControllerTest {

    private static Validator validator;
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private UserTransferService userTransferService;
    @MockitoBean
    private LinkedRequestRepository mockLinkedRequestRepository;

    @Test
    void shouldRenderHomePage() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Nested
    class CheckAnswer {
        @DisplayName("Should render preview with user data")
        @Test
        void shouldRenderPreviewPageWithUserData() throws Exception {
            MvcResult result = mockMvc.perform(post("/check-answers")
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
            MvcResult result = mockMvc.perform(post("/check-answers")
                            .param("additionalInfo", "My surname has changed due to marriage."))
                    .andExpect(status().isOk())
                    .andExpect(view().name("user-transfer-request"))
                    .andExpect(model().hasErrors())
                    .andExpect(model().attributeHasFieldErrors("userTransferRequest", "oldLogin"))
                    .andReturn();

            String html = result.getResponse().getContentAsString();
            assertTrue(html.contains("Enter CCMS username"));
        }
    }

    @Nested
    class LinkUserRequest {
        @DisplayName("Render user transfer form")
        @Test
        void shouldRenderUserTransferForm() throws Exception {
            mockMvc.perform(get("/user-transfer-request")
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
            CcmsUser ccmsUser = CcmsUser.builder()
                    .loginId("Alice")
                    .firstName("Alison")
                    .lastName("Doe")
                    .build();

            LinkedRequest linkedRequest = new LinkedRequest().toBuilder()
                    .additionalInfo("My surname has changed due to marriage.")
                    .ccmsUser(ccmsUser)
                    .status(Status.OPEN)
                    .idamFirstName("TODO in STB-2368")
                    .idamLastName("TODO in STB-2368")
                    .idamLegacyUserId(UUID.randomUUID())
                    .idamEmail(StringUtils.randomAlphanumeric(6))
                    .createdDate(LocalDateTime.now())
                    .build();

            when(mockLinkedRequestRepository.countByCcmsUser_LoginIdAndStatusIn(anyString(), anyList())).thenReturn(0);

            mockMvc.perform(post("/request-confirmation")
                            .param("additionalInfo", "My surname has changed due to marriage."))
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
            var results = mockMvc.perform(post("/request-confirmation")
                            .param("oldLogin", "Alice")
                            .param("additionalInfo", "My surname has changed due to marriage."))
                    .andExpect(status().isOk())
                    .andExpect(view().name("request_rejected"))
                    .andReturn();
        }

        @DisplayName("Should return request accepted when login id not OPEN or APPROVED status")
        @Test
        void shouldReturnSuccessForLoginIdNotInOpenOrApprovedStatus() throws Exception {
            when(mockLinkedRequestRepository.countByCcmsUser_LoginIdAndStatusIn(anyString(), anyList())).thenReturn(0);
            var results = mockMvc.perform(post("/request-confirmation")
                            .param("oldLogin", "Alice")
                            .param("additionalInfo", "My surname has changed due to marriage."))
                    .andExpect(status().isOk())
                    .andExpect(view().name("request-created"))
                    .andReturn();
        }

    }
}