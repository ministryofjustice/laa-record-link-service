package uk.gov.justice.record.link.controller;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.justice.record.link.entity.CcmsUser;
import uk.gov.justice.record.link.entity.LinkedRequest;
import uk.gov.justice.record.link.entity.Status;
import uk.gov.justice.record.link.model.PagedUserRequest;
import uk.gov.justice.record.link.service.LinkedRequestService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@Disabled
@WebMvcTest(ManageLinkingAccountRequestsController.class)
class ManageLinkingAccountRequestsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LinkedRequestService linkedRequestService;

    @Nested
    @DisplayName("ShouldReturnViewWithPaginatedData")
    class GetLinkingRequests {

        @Test
        void calledWithDefaultParameters() throws Exception {
            List<LinkedRequest> mockRequests = createMockLinkedRequests();
            Page<LinkedRequest> mockPage = new PageImpl<>(mockRequests, PageRequest.of(0, 10), 15);

            when(linkedRequestService.getAllLinkingRequests(1, 10)).thenReturn(mockPage);

            mockMvc.perform(get("/manage-linking-account")
                            .param("page", "1")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("manage-link-account-requests"))
                    .andExpect(model().attributeExists("pagedRequest"))
                    .andExpect(result -> {
                        PagedUserRequest<?> pagedRequest = (PagedUserRequest<?>) result.getModelAndView().getModel().get("pagedRequest");
                        assertThat(pagedRequest.currentPage()).isEqualTo(1);
                        assertThat(pagedRequest.totalPages()).isEqualTo(2);
                        assertThat(pagedRequest.totalItems()).isEqualTo(15L);
                        assertThat(pagedRequest.pageSize()).isEqualTo(10);
                        assertThat(pagedRequest.hasNext()).isEqualTo(true);
                        assertThat(pagedRequest.hasPrevious()).isEqualTo(false);
                    });
        }

        @Test
        void calledWithSpecificPageAndSize() throws Exception {
            List<LinkedRequest> mockRequests = createMockLinkedRequests();
            Page<LinkedRequest> mockPage = new PageImpl<>(mockRequests, PageRequest.of(1, 5), 25);

            when(linkedRequestService.getAllLinkingRequests(2, 5)).thenReturn(mockPage);

            mockMvc.perform(get("/manage-linking-account")
                            .param("page", "2")
                            .param("size", "5"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("manage-link-account-requests"))
                    .andExpect(model().attributeExists("pagedRequest"))
                    .andExpect(result -> {
                        PagedUserRequest<?> pagedRequest = (PagedUserRequest<?>) result.getModelAndView().getModel().get("pagedRequest");
                        assertThat(pagedRequest.currentPage()).isEqualTo(2);
                        assertThat(pagedRequest.totalPages()).isEqualTo(5);
                        assertThat(pagedRequest.totalItems()).isEqualTo(25L);
                        assertThat(pagedRequest.pageSize()).isEqualTo(5);
                        assertThat(pagedRequest.hasPrevious()).isEqualTo(true);
                        assertThat(pagedRequest.hasNext()).isEqualTo(true);
                    });
        }

        @Test
        void noLinkedRequestsExist() throws Exception {
            Page<LinkedRequest> emptyPage = new PageImpl<>(Arrays.asList(), PageRequest.of(0, 10), 0);

            when(linkedRequestService.getAllLinkingRequests(1, 10)).thenReturn(emptyPage);

            mockMvc.perform(get("/manage-linking-account")
                            .param("page", "1")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("manage-link-account-requests"))
                    .andExpect(model().attributeExists("pagedRequest"))
                    .andExpect(result -> {
                        PagedUserRequest<?> pagedRequest = (PagedUserRequest<?>) result.getModelAndView().getModel().get("pagedRequest");
                        assertThat(pagedRequest.currentPage()).isEqualTo(1);
                        assertThat(pagedRequest.totalPages()).isEqualTo(0);
                        assertThat(pagedRequest.totalItems()).isEqualTo(0L);
                        assertThat(pagedRequest.pageSize()).isEqualTo(10);
                        assertThat(pagedRequest.hasPrevious()).isEqualTo(false);
                        assertThat(pagedRequest.hasNext()).isEqualTo(false);
                    });
        }

        @Test
        void requestingFinalPageOfResults() throws Exception {
            List<LinkedRequest> mockRequests = createMockLinkedRequests();
            Page<LinkedRequest> mockPage = new PageImpl<>(mockRequests, PageRequest.of(2, 10), 23);

            when(linkedRequestService.getAllLinkingRequests(3, 10)).thenReturn(mockPage);

            mockMvc.perform(get("/manage-linking-account")
                            .param("page", "3")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("manage-link-account-requests"))
                    .andExpect(model().attributeExists("pagedRequest"))
                    .andExpect(result -> {
                        PagedUserRequest<?> pagedRequest = (PagedUserRequest<?>) result.getModelAndView().getModel().get("pagedRequest");
                        assertThat(pagedRequest.currentPage()).isEqualTo(3);
                        assertThat(pagedRequest.totalPages()).isEqualTo(3);
                        assertThat(pagedRequest.totalItems()).isEqualTo(23L);
                        assertThat(pagedRequest.hasPrevious()).isEqualTo(true);
                        assertThat(pagedRequest.hasNext()).isEqualTo(false);
                    });
        }

        @Test
        void calledSuccessfully() throws Exception {
            List<LinkedRequest> mockRequests = createMockLinkedRequests();
            Page<LinkedRequest> mockPage = new PageImpl<>(mockRequests, PageRequest.of(0, 10), 15);

            when(linkedRequestService.getAllLinkingRequests(1, 10)).thenReturn(mockPage);

            mockMvc.perform(get("/manage-linking-account")
                            .param("page", "1")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("manage-link-account-requests"))
                    .andExpect(model().attributeExists("pagedRequest"))
                    .andExpect(result -> {
                        PagedUserRequest<?> pagedRequest = (PagedUserRequest<?>) result.getModelAndView().getModel().get("pagedRequest");
                        assertThat(pagedRequest.linkedRequests()).isNotNull();
                        assertThat(pagedRequest.currentPage()).isNotNull();
                        assertThat(pagedRequest.totalPages()).isNotNull();
                        assertThat(pagedRequest.totalItems()).isNotNull();
                        assertThat(pagedRequest.pageSize()).isNotNull();
                        assertThat(pagedRequest.hasPrevious()).isNotNull();
                        assertThat(pagedRequest.hasNext()).isNotNull();
                    });
        }

        private List<LinkedRequest> createMockLinkedRequests() {
            CcmsUser ccmsUser1 = CcmsUser.builder()
                    .loginId("user1")
                    .firstName("John")
                    .lastName("Doe")
                    .firmCode("FIRM001")
                    .email("john.doe@example.com")
                    .build();

            CcmsUser ccmsUser2 = CcmsUser.builder()
                    .loginId("user2")
                    .firstName("Jane")
                    .lastName("Smith")
                    .firmCode("FIRM002")
                    .email("jane.smith@example.com")
                    .build();

            LinkedRequest request1 = LinkedRequest.builder()
                    .ccmsUser(ccmsUser1)
                    .idamLegacyUserId(UUID.randomUUID())
                    .idamFirstName("Alice")
                    .idamLastName("Johnson")
                    .idamFirmName("Johnson & Associates")
                    .idamFirmCode("JA001")
                    .idamEmail("alice.johnson@example.com")
                    .createdDate(LocalDateTime.now().minusDays(5))
                    .status(Status.OPEN)
                    .build();

            LinkedRequest request2 = LinkedRequest.builder()
                    .ccmsUser(ccmsUser2)
                    .idamLegacyUserId(UUID.randomUUID())
                    .idamFirstName("Bob")
                    .idamLastName("Wilson")
                    .idamFirmName("Wilson Legal")
                    .idamFirmCode("WL001")
                    .idamEmail("bob.wilson@example.com")
                    .createdDate(LocalDateTime.now().minusDays(3))
                    .status(Status.APPROVED)
                    .build();

            LinkedRequest request3 = LinkedRequest.builder()
                    .ccmsUser(ccmsUser1)
                    .idamLegacyUserId(UUID.randomUUID())
                    .idamFirstName("Carol")
                    .idamLastName("Brown")
                    .idamFirmName("Brown Law Firm")
                    .idamFirmCode("BLF001")
                    .idamEmail("carol.brown@example.com")
                    .createdDate(LocalDateTime.now().minusDays(1))
                    .status(Status.REJECTED)
                    .build();

            return Arrays.asList(request1, request2, request3);
        }
    }
}
