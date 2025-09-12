package uk.gov.justice.record.link.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.justice.record.link.config.DevSecurityConfig;
import uk.gov.justice.record.link.constants.SilasConstants;
import uk.gov.justice.record.link.entity.CcmsUser;
import uk.gov.justice.record.link.entity.LinkedRequest;
import uk.gov.justice.record.link.entity.Status;
import uk.gov.justice.record.link.model.PagedUserRequest;
import uk.gov.justice.record.link.service.LinkedRequestService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ActiveProfiles("local")
@WebMvcTest(ManageLinkingAccountRequestsController.class)
@Import(DevSecurityConfig.class)
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
            List<LinkedRequest> mockAssignedRequests = createMockAssignedRequests();
            Page<LinkedRequest> mockPage = new PageImpl<>(mockRequests, PageRequest.of(0, 10), 15);
            Page<LinkedRequest> mockAssignedPage = new PageImpl<>(mockAssignedRequests, PageRequest.of(0, 10), 5);

            when(linkedRequestService.getLinkingRequestByOldLogin("", 1, 10)).thenReturn(mockPage);
            when(linkedRequestService.getAssignedRequests("1234567890", 1, 10)).thenReturn(mockAssignedPage);

            mockMvc.perform(get("/internal/manage-linking-account")
                            .with(oidcLogin()
                                    .idToken(token -> token.claims(
                                            claim -> {
                                                claim.put(SilasConstants.FIRST_NAME, "Jane");
                                                claim.put(SilasConstants.SURNAME, "Doe");
                                                claim.put(SilasConstants.SILAS_LOGIN_ID, "1234567890");
                                                claim.put(SilasConstants.USER_EMAIL, "janedoe@test.com");
                                            }
                                    ))
                                    .authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER")))
                            .param("page", "1")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("manage-link-account-requests"))
                    .andExpect(model().attributeExists("pagedRequest"))
                    .andExpect(model().attributeExists("assignedPagedRequest"))
                    .andExpect(result -> {
                        PagedUserRequest<?> pagedRequest = (PagedUserRequest<?>) Objects.requireNonNull(result.getModelAndView()).getModel().get("pagedRequest");
                        assertThat(pagedRequest.currentPage()).isEqualTo(1);
                        assertThat(pagedRequest.totalPages()).isEqualTo(2);
                        assertThat(pagedRequest.totalItems()).isEqualTo(15L);
                        assertThat(pagedRequest.pageSize()).isEqualTo(10);
                        assertThat(pagedRequest.hasNext()).isEqualTo(true);
                        assertThat(pagedRequest.hasPrevious()).isEqualTo(false);
                        
                        PagedUserRequest<?> assignedPagedRequest = (PagedUserRequest<?>) Objects.requireNonNull(result.getModelAndView()).getModel().get("assignedPagedRequest");
                        assertThat(assignedPagedRequest.currentPage()).isEqualTo(1);
                        assertThat(assignedPagedRequest.totalPages()).isEqualTo(1);
                        assertThat(assignedPagedRequest.totalItems()).isEqualTo(2L);
                        assertThat(assignedPagedRequest.pageSize()).isEqualTo(10);
                        assertThat(assignedPagedRequest.hasNext()).isEqualTo(false);
                        assertThat(assignedPagedRequest.hasPrevious()).isEqualTo(false);
                    });
        }

        @Test
        void calledWithSpecificPageAndSize() throws Exception {
            List<LinkedRequest> mockRequests = createMockLinkedRequests();
            Page<LinkedRequest> mockPage = new PageImpl<>(mockRequests, PageRequest.of(1, 5), 25);
            Page<LinkedRequest> emptyAssignedPage = new PageImpl<>(List.of(), PageRequest.of(0, 5), 0);

            when(linkedRequestService.getLinkingRequestByOldLogin("", 2, 5)).thenReturn(mockPage);
            when(linkedRequestService.getAssignedRequests("1234567890", 1, 5)).thenReturn(emptyAssignedPage);

            mockMvc.perform(get("/internal/manage-linking-account")
                            .with(oidcLogin()
                                    .idToken(token -> token.claims(
                                            claim -> {
                                                claim.put(SilasConstants.FIRST_NAME, "Jane");
                                                claim.put(SilasConstants.SURNAME, "Doe");
                                                claim.put(SilasConstants.SILAS_LOGIN_ID, "1234567890");
                                                claim.put(SilasConstants.USER_EMAIL, "janedoe@test.com");
                                            }
                                    ))
                                    .authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER")))
                            .param("page", "2")
                            .param("size", "5"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("manage-link-account-requests"))
                    .andExpect(model().attributeExists("pagedRequest"))
                    .andExpect(model().attributeExists("assignedPagedRequest"))
                    .andExpect(result -> {
                        PagedUserRequest<?> pagedRequest = (PagedUserRequest<?>) Objects.requireNonNull(result.getModelAndView()).getModel().get("pagedRequest");
                        assertThat(pagedRequest.currentPage()).isEqualTo(2);
                        assertThat(pagedRequest.pageSize()).isEqualTo(5);
                        assertThat(pagedRequest.totalItems()).isEqualTo(25L);
                        assertThat(pagedRequest.hasPrevious()).isEqualTo(true);
                        assertThat(pagedRequest.hasNext()).isEqualTo(true);
                        
                        // Verify assigned requests are also present
                        PagedUserRequest<?> assignedPagedRequest = (PagedUserRequest<?>) Objects.requireNonNull(result.getModelAndView()).getModel().get("assignedPagedRequest");
                        assertThat(assignedPagedRequest).isNotNull();
                        assertThat(assignedPagedRequest.totalItems()).isEqualTo(0L);
                    });
        }

        @Test
        void noLinkedRequestsExist() throws Exception {
            Page<LinkedRequest> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
            Page<LinkedRequest> emptyAssignedPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

            when(linkedRequestService.getLinkingRequestByOldLogin("", 1, 10)).thenReturn(emptyPage);
            when(linkedRequestService.getAssignedRequests("1234567890", 1, 10)).thenReturn(emptyAssignedPage);

            mockMvc.perform(get("/internal/manage-linking-account")
                            .with(oidcLogin()
                                    .idToken(token -> token.claims(
                                            claim -> {
                                                claim.put(SilasConstants.FIRST_NAME, "Jane");
                                                claim.put(SilasConstants.SURNAME, "Doe");
                                                claim.put(SilasConstants.SILAS_LOGIN_ID, "1234567890");
                                                claim.put(SilasConstants.USER_EMAIL, "janedoe@test.com");
                                            }
                                    ))
                                    .authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER")))
                            .param("page", "1")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("manage-link-account-requests"))
                    .andExpect(model().attributeExists("pagedRequest"))
                    .andExpect(result -> {
                        PagedUserRequest<?> pagedRequest = (PagedUserRequest<?>) Objects.requireNonNull(result.getModelAndView()).getModel().get("pagedRequest");
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
            Page<LinkedRequest> emptyAssignedPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

            when(linkedRequestService.getLinkingRequestByOldLogin("", 3, 10)).thenReturn(mockPage);
            when(linkedRequestService.getAssignedRequests("1234567890", 1, 10)).thenReturn(emptyAssignedPage);

            mockMvc.perform(get("/internal/manage-linking-account")
                            .with(oidcLogin()
                                    .idToken(token -> token.claims(
                                            claim -> {
                                                claim.put(SilasConstants.FIRST_NAME, "Jane");
                                                claim.put(SilasConstants.SURNAME, "Doe");
                                                claim.put(SilasConstants.SILAS_LOGIN_ID, "1234567890");
                                                claim.put(SilasConstants.USER_EMAIL, "janedoe@test.com");
                                            }
                                    ))
                                    .authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER")))
                            .param("page", "3")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("manage-link-account-requests"))
                    .andExpect(model().attributeExists("pagedRequest"))
                    .andExpect(result -> {
                        PagedUserRequest<?> pagedRequest = (PagedUserRequest<?>) Objects.requireNonNull(result.getModelAndView()).getModel().get("pagedRequest");
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
            Page<LinkedRequest> emptyAssignedPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

            when(linkedRequestService.getLinkingRequestByOldLogin("", 1, 10)).thenReturn(mockPage);
            when(linkedRequestService.getAssignedRequests("1234567890", 1, 10)).thenReturn(emptyAssignedPage);

            mockMvc.perform(get("/internal/manage-linking-account")
                            .with(oidcLogin()
                                    .idToken(token -> token.claims(
                                            claim -> {
                                                claim.put(SilasConstants.FIRST_NAME, "Jane");
                                                claim.put(SilasConstants.SURNAME, "Doe");
                                                claim.put(SilasConstants.SILAS_LOGIN_ID, "1234567890");
                                                claim.put(SilasConstants.USER_EMAIL, "janedoe@test.com");
                                            }
                                    ))
                                    .authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER")))
                            .param("page", "1")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("manage-link-account-requests"))
                    .andExpect(model().attributeExists("pagedRequest"))
                    .andExpect(result -> {
                        PagedUserRequest<?> pagedRequest = (PagedUserRequest<?>) Objects.requireNonNull(result.getModelAndView()).getModel().get("pagedRequest");
                        assertThat(pagedRequest.linkedRequests()).isNotNull();
                        assertThat(pagedRequest.currentPage()).isNotNull();
                        assertThat(pagedRequest.totalPages()).isNotNull();
                        assertThat(pagedRequest.totalItems()).isNotNull();
                        assertThat(pagedRequest.pageSize()).isNotNull();
                        assertThat(pagedRequest.hasPrevious()).isNotNull();
                        assertThat(pagedRequest.hasNext()).isNotNull();
                    });

        }

        @DisplayName("Should return manage link account page when called with old login id")
        @Test
        void calledWithLoginId() throws Exception {
            List<LinkedRequest> mockRequests = createMockLinkedRequests();
            Page<LinkedRequest> mockPage = new PageImpl<>(mockRequests, PageRequest.of(0, 10), 15);
            Page<LinkedRequest> emptyAssignedPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

            when(linkedRequestService.getLinkingRequestByOldLogin("testLogin", 1, 10)).thenReturn(mockPage);
            when(linkedRequestService.getAssignedRequests("1234567890", 1, 10)).thenReturn(emptyAssignedPage);

            mockMvc.perform(get("/internal/manage-linking-account")
                            .with(oidcLogin()
                                    .idToken(token -> token.claims(
                                            claim -> {
                                                claim.put(SilasConstants.FIRST_NAME, "Jane");
                                                claim.put(SilasConstants.SURNAME, "Doe");
                                                claim.put(SilasConstants.SILAS_LOGIN_ID, "1234567890");
                                                claim.put(SilasConstants.USER_EMAIL, "janedoe@test.com");
                                            }
                                    ))
                                    .authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER")))
                            .param("page", "1")
                            .param("size", "10")
                            .param("oldLoginId", "testLogin"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("manage-link-account-requests"))
                    .andExpect(model().attributeExists("pagedRequest"))
                    .andExpect(result -> {
                        PagedUserRequest<?> pagedRequest = (PagedUserRequest<?>) Objects.requireNonNull(result.getModelAndView()).getModel().get("pagedRequest");
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
                    .idamLegacyUserId(UUID.randomUUID().toString())
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
                    .idamLegacyUserId(UUID.randomUUID().toString())
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
                    .idamLegacyUserId(UUID.randomUUID().toString())
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

        private List<LinkedRequest> createMockAssignedRequests() {
            CcmsUser ccmsUser1 = CcmsUser.builder()
                    .loginId("assignedUser1")
                    .firstName("Assigned")
                    .lastName("User1")
                    .firmCode("FIRM003")
                    .email("assigned.user1@example.com")
                    .build();

            LinkedRequest assignedRequest1 = LinkedRequest.builder()
                    .ccmsUser(ccmsUser1)
                    .idamLegacyUserId(UUID.randomUUID().toString())
                    .idamFirstName("Assigned")
                    .idamLastName("Person1")
                    .idamFirmName("Assigned Firm 1")
                    .idamFirmCode("AF001")
                    .idamEmail("assigned.person1@example.com")
                    .createdDate(LocalDateTime.now().minusDays(2))
                    .assignedDate(LocalDateTime.now().minusDays(1))
                    .laaAssignee("1234567890")
                    .status(Status.OPEN)
                    .build();

            LinkedRequest assignedRequest2 = LinkedRequest.builder()
                    .ccmsUser(ccmsUser1)
                    .idamLegacyUserId(UUID.randomUUID().toString())
                    .idamFirstName("Assigned")
                    .idamLastName("Person2")
                    .idamFirmName("Assigned Firm 2")
                    .idamFirmCode("AF002")
                    .idamEmail("assigned.person2@example.com")
                    .createdDate(LocalDateTime.now().minusDays(3))
                    .assignedDate(LocalDateTime.now().minusDays(2))
                    .laaAssignee("1234567890")
                    .status(Status.APPROVED)
                    .build();

            return Arrays.asList(assignedRequest1, assignedRequest2);
        }
    }

    @Nested
    @DisplayName("AssignedRequestsAndPagination")
    class AssignedRequestsAndPagination {

        @Test
        void shouldReturnAssignedRequestsWithSeparatePagination() throws Exception {
            List<LinkedRequest> mockRequests = createMockLinkedRequests();
            List<LinkedRequest> mockAssignedRequests = createMockAssignedRequests();
            Page<LinkedRequest> mockPage = new PageImpl<>(mockRequests, PageRequest.of(0, 10), 20);
            Page<LinkedRequest> mockAssignedPage = new PageImpl<>(mockAssignedRequests, PageRequest.of(1, 10), 11);

            when(linkedRequestService.getLinkingRequestByOldLogin("", 1, 10)).thenReturn(mockPage);
            when(linkedRequestService.getAssignedRequests("1234567890", 2, 10)).thenReturn(mockAssignedPage);

            mockMvc.perform(get("/internal/manage-linking-account")
                            .with(oidcLogin()
                                    .idToken(token -> token.claims(
                                            claim -> {
                                                claim.put(SilasConstants.FIRST_NAME, "Jane");
                                                claim.put(SilasConstants.SURNAME, "Doe");
                                                claim.put(SilasConstants.SILAS_LOGIN_ID, "1234567890");
                                                claim.put(SilasConstants.USER_EMAIL, "janedoe@test.com");
                                            }
                                    ))
                                    .authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER")))
                            .param("page", "1")
                            .param("size", "10")
                            .param("assignedPage", "2"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("manage-link-account-requests"))
                    .andExpect(model().attributeExists("assignedPagedRequest"))
                    .andExpect(result -> {
                        PagedUserRequest<?> assignedPagedRequest = (PagedUserRequest<?>) Objects.requireNonNull(result.getModelAndView()).getModel().get("assignedPagedRequest");
                        assertThat(assignedPagedRequest.currentPage()).isEqualTo(2);
                        assertThat(assignedPagedRequest.totalPages()).isEqualTo(2);
                        assertThat(assignedPagedRequest.totalItems()).isEqualTo(11L);
                        assertThat(assignedPagedRequest.pageSize()).isEqualTo(10);
                        assertThat(assignedPagedRequest.hasNext()).isEqualTo(false);
                        assertThat(assignedPagedRequest.hasPrevious()).isEqualTo(true);
                    });
        }

        @Test
        void shouldHandleEmptyAssignedRequests() throws Exception {
            List<LinkedRequest> mockRequests = createMockLinkedRequests();
            Page<LinkedRequest> mockPage = new PageImpl<>(mockRequests, PageRequest.of(0, 10), 15);
            Page<LinkedRequest> emptyAssignedPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

            when(linkedRequestService.getLinkingRequestByOldLogin("", 1, 10)).thenReturn(mockPage);
            when(linkedRequestService.getAssignedRequests("1234567890", 1, 10)).thenReturn(emptyAssignedPage);

            mockMvc.perform(get("/internal/manage-linking-account")
                            .with(oidcLogin()
                                    .idToken(token -> token.claims(
                                            claim -> {
                                                claim.put(SilasConstants.FIRST_NAME, "Jane");
                                                claim.put(SilasConstants.SURNAME, "Doe");
                                                claim.put(SilasConstants.SILAS_LOGIN_ID, "1234567890");
                                                claim.put(SilasConstants.USER_EMAIL, "janedoe@test.com");
                                            }
                                    ))
                                    .authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"))))
                    .andExpect(status().isOk())
                    .andExpect(view().name("manage-link-account-requests"))
                    .andExpect(model().attributeExists("assignedPagedRequest"))
                    .andExpect(result -> {
                        PagedUserRequest<?> assignedPagedRequest = (PagedUserRequest<?>) Objects.requireNonNull(result.getModelAndView()).getModel().get("assignedPagedRequest");
                        assertThat(assignedPagedRequest.totalItems()).isEqualTo(0L);
                        assertThat(assignedPagedRequest.linkedRequests()).isEmpty();
                    });
        }



        private List<LinkedRequest> createMockAssignedRequests() {
            CcmsUser ccmsUser1 = CcmsUser.builder()
                    .loginId("assignedUser1")
                    .firstName("Assigned")
                    .lastName("User1")
                    .firmCode("FIRM003")
                    .email("assigned.user1@example.com")
                    .build();

            LinkedRequest assignedRequest1 = LinkedRequest.builder()
                    .ccmsUser(ccmsUser1)
                    .idamLegacyUserId(UUID.randomUUID().toString())
                    .idamFirstName("Assigned")
                    .idamLastName("Person1")
                    .idamFirmName("Assigned Firm 1")
                    .idamFirmCode("AF001")
                    .idamEmail("assigned.person1@example.com")
                    .createdDate(LocalDateTime.now().minusDays(2))
                    .assignedDate(LocalDateTime.now().minusDays(1))
                    .laaAssignee("1234567890")
                    .status(Status.OPEN)
                    .build();

            return Arrays.asList(assignedRequest1);
        }
    }

    @Nested
    @DisplayName("viewUserDetails")
    class ViewUserDetails {

        @Test
        void shouldShowRequestDetails() throws Exception {
            List<LinkedRequest> mockRequests = createMockLinkedRequests();
            when(linkedRequestService.getRequestById(anyString())).thenReturn(Optional.of(mockRequests.get(0)));

            mockMvc.perform(get("/internal/manage-linking-account/check-user-details")
                            .param("id", "25985641-9ba5-44a1-7e8f-e23u7be7bb0l"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("check-user-details"))
                    .andExpect(model().attributeExists("user"))
                    .andExpect(model().attributeExists("ccmsuser"));
        }

        @Test
        void shouldShowRequestDetailsIfNullUser() throws Exception {
            List<LinkedRequest> mockRequests = createMockLinkedRequestsWithNullUser();
            when(linkedRequestService.getRequestById(anyString())).thenReturn(Optional.of(mockRequests.get(0)));

            mockMvc.perform(get("/internal/manage-linking-account/check-user-details")
                            .param("id", "25985641-9ba5-44a1-7e8f-e23u7be7bb0l"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("check-user-details"))
                    .andExpect(model().attributeExists("user"))
                    .andExpect(result -> {
                        CcmsUser ccmsuser = (CcmsUser) result.getModelAndView().getModel().get("ccmsuser");
                        assertThat(ccmsuser).isNull();
                    });
        }
    }

    private List<LinkedRequest> createMockLinkedRequests() {
        CcmsUser ccmsUser1 = CcmsUser.builder()
                .loginId("user1")
                .firstName("John")
                .lastName("Doe")
                .firmCode("FIRM001")
                .email("john.doe@example.com")
                .build();

        LinkedRequest request1 = LinkedRequest.builder()
                .ccmsUser(ccmsUser1)
                .idamLegacyUserId(UUID.randomUUID().toString())
                .idamFirstName("Alice")
                .idamLastName("Johnson")
                .idamFirmName("Johnson & Associates")
                .idamFirmCode("JA001")
                .idamEmail("alice.johnson@example.com")
                .createdDate(LocalDateTime.now().minusDays(5))
                .status(Status.OPEN)
                .build();

        return Arrays.asList(request1);
    }

    private List<LinkedRequest> createMockLinkedRequestsWithNullUser() {

        LinkedRequest request1 = LinkedRequest.builder()
                .ccmsUser(null)
                .idamLegacyUserId(UUID.randomUUID().toString())
                .idamFirstName("Alice")
                .idamLastName("Johnson")
                .idamFirmName("Johnson & Associates")
                .idamFirmCode("JA001")
                .idamEmail("alice.johnson@example.com")
                .createdDate(LocalDateTime.now().minusDays(5))
                .status(Status.OPEN)
                .build();

        return Arrays.asList(request1);
    }
}
