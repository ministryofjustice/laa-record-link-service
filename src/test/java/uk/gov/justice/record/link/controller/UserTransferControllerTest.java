package uk.gov.justice.record.link.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
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
import uk.gov.justice.record.link.service.UserTransferService;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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

            when(userTransferService.save(any(UserTransferRequest.class))).thenReturn(linkedRequest);

            mockMvc.perform(get("/request-confirmation")
                            .param("oldLogin", "Alice")
                            .param("additionalInfo", "My surname has changed due to marriage."))
                    .andExpect(status().isOk())
                    .andExpect(view().name("request-created"))
                    .andExpect(model().attributeExists("userTransferRequest"))
                    .andReturn();

            verify(userTransferService, times(1)).save(any(UserTransferRequest.class));
        }

        @DisplayName("User transfer form with empty CCMS login id")
        @Test
        void givenEmptyUsername_shouldTriggerPatternViolation() throws Exception {

            ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
            validator = factory.getValidator();

            UserTransferRequest transferRequest = new UserTransferRequest();
            transferRequest.setAdditionalInfo("My surname has changed due to marriage.");
            Set<ConstraintViolation<UserTransferRequest>> violations = validator.validate(transferRequest);
            assertThat(violations).extracting(ConstraintViolation::getMessage)
                    .contains("Enter CCMS username",
                            "Enter CCMS username");
        }
    }
}