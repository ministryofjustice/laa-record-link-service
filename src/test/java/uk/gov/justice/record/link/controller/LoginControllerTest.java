package uk.gov.justice.record.link.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(LoginController.class)
public class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldLoadHomePageSuccessfully() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Nested
    class checkAnswer {
        @DisplayName("Should render preview with user data")
        @Test
        void shouldRenderPreviewPageWithUserData() throws Exception {
            MvcResult result = mockMvc.perform(post("/check-answers")
                            .param("oldLogin", "Alice")
                            .param("additionalInfo", "My surname has changed due to marriage."))
                    .andExpect(status().isOk())
                    .andExpect(view().name("check-answers"))
                    .andExpect(model().attributeExists("user"))
                    .andReturn();
            String html = result.getResponse().getContentAsString();

            assertTrue(html.contains("Alice"));
            assertTrue(html.contains("My surname has changed due to marriage."));
        }
    }

    @Nested
    class linkUserRequest {
        @DisplayName("Should render link user form")
        @Test
        void shouldRenderLinkUserForm() throws Exception {
            mockMvc.perform(get("/login")
                            .param("oldLogin", "Alice")
                            .param("additionalInfo", "My surname has changed due to marriage."))
                    .andExpect(status().isOk())
                    .andExpect(view().name("link-user"))
                    .andExpect(model().attributeExists("user"))
                    .andReturn();
        }
    }
}