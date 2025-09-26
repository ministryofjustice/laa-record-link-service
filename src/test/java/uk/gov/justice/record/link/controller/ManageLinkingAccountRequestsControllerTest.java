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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

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

            when(linkedRequestService.searchLinkingRequests("", 1, 10)).thenReturn(mockPage);
            when(linkedRequestService.getAssignedRequests("janedoe@test.com", 1, 10)).thenReturn(mockAssignedPage);

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

            when(linkedRequestService.searchLinkingRequests("", 2, 5)).thenReturn(mockPage);
            when(linkedRequestService.getAssignedRequests("janedoe@test.com", 1, 5)).thenReturn(emptyAssignedPage);

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

            when(linkedRequestService.searchLinkingRequests("", 1, 10)).thenReturn(emptyPage);
            when(linkedRequestService.getAssignedRequests("janedoe@test.com", 1, 10)).thenReturn(emptyAssignedPage);

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

            when(linkedRequestService.searchLinkingRequests("", 3, 10)).thenReturn(mockPage);
            when(linkedRequestService.getAssignedRequests("janedoe@test.com", 1, 10)).thenReturn(emptyAssignedPage);

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

            when(linkedRequestService.searchLinkingRequests("", 1, 10)).thenReturn(mockPage);
            when(linkedRequestService.getAssignedRequests("janedoe@test.com", 1, 10)).thenReturn(emptyAssignedPage);

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

            when(linkedRequestService.searchLinkingRequests("testLogin", 1, 10)).thenReturn(mockPage);
            when(linkedRequestService.getAssignedRequests("janedoe@test.com", 1, 10)).thenReturn(emptyAssignedPage);

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
                            .param("searchTerm", "testLogin"))
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
                    .id(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                    .ccmsUser(ccmsUser1)
                    .oldLoginId("assigned_user1_login")
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
                    .id(UUID.fromString("22222222-2222-2222-2222-222222222222"))
                    .ccmsUser(ccmsUser1)
                    .oldLoginId("assigned_user2_login")
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
    @DisplayName("CanDownloadLinkAccountData")
    class DownloadLinkAccountData {

        @Test
        void shouldReturnAllLinkedAccounts() {
            LinkedRequest request1 = LinkedRequest.builder().oldLoginId("login1").build();
            LinkedRequest request2 = LinkedRequest.builder().oldLoginId("login2").build();
            List<LinkedRequest> mockList = List.of(request1, request2);

            when(linkedRequestService.getAllLinkedAccounts()).thenReturn(mockList);

            List<LinkedRequest> result = linkedRequestService.getAllLinkedAccounts();

            assertThat(result).containsExactlyElementsOf(mockList);
            verify(linkedRequestService).getAllLinkedAccounts();
        }

        @Test
        void shouldDownloadCsvWithAccountData() throws Exception {
            LinkedRequest request = LinkedRequest.builder()
                    .oldLoginId("old_login_1")
                    .idamFirmName("Firm Name")
                    .additionalInfo("Vendor123")
                    .createdDate(LocalDateTime.of(2024, 6, 1, 0, 0))
                    .assignedDate(LocalDateTime.of(2024, 6, 2, 0, 0))
                    .decisionDate(LocalDateTime.of(2024, 6, 3, 0, 0))
                    .status(Status.APPROVED)
                    .decisionReason("Valid")
                    .laaAssignee("assignee1")
                    .ccmsUser(CcmsUser.builder().loginId("login_1").build())
                    .build();

            when(linkedRequestService.getAllLinkedAccounts()).thenReturn(List.of(request));

            mockMvc.perform(get("/internal/download-link-account-data"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Type", "text/csv; charset=UTF-8"))
                    .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.startsWith("attachment; filename=\"account_transfer_")))
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("provided_old_login_id,firm_name,vendor_site_code,creation_date,assigned_date,decision_date,status,decision_reason,laa_assignee,login_id")))
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("old_login_1,Firm Name,Vendor123,2024-06-01,2024-06-02,2024-06-03,APPROVED,Valid,assignee1,login_1")));
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

            when(linkedRequestService.searchLinkingRequests("", 1, 10)).thenReturn(mockPage);
            when(linkedRequestService.getAssignedRequests("janedoe@test.com", 2, 10)).thenReturn(mockAssignedPage);

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

            when(linkedRequestService.searchLinkingRequests("", 1, 10)).thenReturn(mockPage);
            when(linkedRequestService.getAssignedRequests("janedoe@test.com", 1, 10)).thenReturn(emptyAssignedPage);

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
                    .id(UUID.fromString("33333333-3333-3333-3333-333333333333"))
                    .ccmsUser(ccmsUser1)
                    .oldLoginId("assigned_user1_login")
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
                            .param("id", "25985641-9ba5-44a1-7e8f-e23u7be7bb0l")
                            .with(oidcLogin()
                                    .idToken(token -> token.claims(
                                            claim -> {
                                                claim.put(SilasConstants.USER_EMAIL, "test.user@example.com");
                                            }
                                    ))
                                    .authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"))))
                    .andExpect(status().isOk())
                    .andExpect(view().name("check-user-details"))
                    .andExpect(model().attributeExists("user"))
                    .andExpect(model().attributeExists("ccmsuser"))
                    .andExpect(model().attribute("loggedinUserEmail", "test.user@example.com"));
        }

        @Test
        void shouldReturnViewUserDetailsPageWithUserDetails() throws Exception {
            LinkedRequest linkedRequest = createMockLinkedRequests().get(0);
            when(linkedRequestService.getRequestById(anyString())).thenReturn(Optional.of(linkedRequest));

            mockMvc.perform(get("/internal/manage-linking-account/check-user-details")
                            .param("id", linkedRequest.id.toString())
                            .with(oidcLogin()
                                    .idToken(token -> token.claims(
                                            claim -> {
                                                claim.put(SilasConstants.USER_EMAIL, "test@example.com");
                                            }
                                    ))
                                    .authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"))))
                    .andExpect(status().isOk())
                    .andExpect(view().name("check-user-details"))
                    .andExpect(model().attribute("user", linkedRequest))
                    .andExpect(model().attribute("loggedinUserEmail", "test@example.com"));
        }

        @Test
        void shouldShowRequestDetailsIfNullUser() throws Exception {
            List<LinkedRequest> mockRequests = createMockLinkedRequestsWithNullUser();
            when(linkedRequestService.getRequestById(anyString())).thenReturn(Optional.of(mockRequests.get(0)));

            mockMvc.perform(get("/internal/manage-linking-account/check-user-details")
                            .param("id", "25985641-9ba5-44a1-7e8f-e23u7be7bb0l")
                            .with(oidcLogin()
                                    .idToken(token -> token.claims(
                                            claim -> {
                                                claim.put(SilasConstants.USER_EMAIL, "test.user@example.com");
                                            }
                                    ))
                                    .authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"))))
                    .andExpect(status().isOk())
                    .andExpect(view().name("check-user-details"))
                    .andExpect(model().attributeExists("user"))
                    .andExpect(model().attribute("loggedinUserEmail", "test.user@example.com"))
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
                .id(UUID.fromString("12345678-1234-1234-1234-123456789012"))
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

    @Nested
    @DisplayName("AssignNextCase")
    class AssignNextCase {

        @Test
        void shouldAssignNextCaseAndRedirectToRequestDetails() throws Exception {
            LinkedRequest assignedRequest = createMockAssignedRequest();
            when(linkedRequestService.assignNextCase("test.user@example.com")).thenReturn(Optional.of(assignedRequest));

            mockMvc.perform(post("/internal/assign-next-case")
                            .with(oidcLogin()
                                    .idToken(token -> token.claims(
                                            claim -> {
                                                claim.put(SilasConstants.FIRST_NAME, "Test");
                                                claim.put(SilasConstants.SURNAME, "User");
                                                claim.put(SilasConstants.SILAS_LOGIN_ID, "asdasdasd");
                                                claim.put(SilasConstants.USER_EMAIL, "test.user@example.com");
                                            }
                                    ))
                                    .authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"))))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/internal/manage-linking-account/check-user-details?id=" + assignedRequest.id))
                    .andExpect(flash().attribute("assignmentSuccess", true));
        }

        @Test
        void shouldRedirectToManagePageWhenNoRequestsAvailable() throws Exception {
            when(linkedRequestService.assignNextCase("asdasdasd")).thenReturn(Optional.empty());

            mockMvc.perform(post("/internal/assign-next-case")
                            .with(oidcLogin()
                                    .idToken(token -> token.claims(
                                            claim -> {
                                                claim.put(SilasConstants.FIRST_NAME, "Test");
                                                claim.put(SilasConstants.SURNAME, "User");
                                                claim.put(SilasConstants.SILAS_LOGIN_ID, "asdasdasd");
                                                claim.put(SilasConstants.USER_EMAIL, "test.user@example.com");
                                            }
                                    ))
                                    .authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"))))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/internal/manage-linking-account"))
                    .andExpect(flash().attribute("noRequestsAvailable", true));
        }

        private LinkedRequest createMockAssignedRequest() {
            CcmsUser ccmsUser = CcmsUser.builder()
                    .loginId("assigned_user")
                    .firstName("Assigned")
                    .lastName("User")
                    .firmCode("FIRM001")
                    .email("assigned.user@example.com")
                    .build();

            return LinkedRequest.builder()
                    .id(UUID.fromString("12345678-1234-1234-1234-123456789012"))
                    .ccmsUser(ccmsUser)
                    .oldLoginId("assigned_login")
                    .idamLegacyUserId(UUID.randomUUID().toString())
                    .idamFirstName("Assigned")
                    .idamLastName("Person")
                    .idamFirmName("Assigned Firm")
                    .idamFirmCode("AF001")
                    .idamEmail("assigned.person@example.com")
                    .createdDate(LocalDateTime.now().minusDays(2))
                    .assignedDate(LocalDateTime.now())
                    .laaAssignee("asdasdasd")
                    .status(Status.OPEN)
                    .build();
        }
    }

    @Nested
    @DisplayName("DecisionReason")
    class DecisionReason {
        @Test
        void shouldProcessDecisionAndShowDecisionReasonPage() throws Exception {
            LinkedRequest linkedRequest = createMockLinkedRequests().get(0);
            when(linkedRequestService.getRequestById(anyString())).thenReturn(Optional.of(linkedRequest));

            mockMvc.perform(post("/internal/manage-linking-account/manage/{id}/decision", linkedRequest.id)
                            .param("decision", "APPROVED")
                            .with(oidcLogin()
                                    .idToken(token -> token.claims(
                                            claim -> {
                                                claim.put(SilasConstants.USER_EMAIL, "test@example.com");
                                            }
                                    ))
                                    .authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"))))
                    .andExpect(status().isOk())
                    .andExpect(view().name("decision-reason"))
                    .andExpect(model().attribute("user", linkedRequest))
                    .andExpect(model().attribute("decision", "APPROVED"));
        }

        @Test
        void shouldProcessDecisionForRejection() throws Exception {
            LinkedRequest linkedRequest = createMockLinkedRequests().get(0);
            when(linkedRequestService.getRequestById(anyString())).thenReturn(Optional.of(linkedRequest));

            mockMvc.perform(post("/internal/manage-linking-account/manage/{id}/decision", linkedRequest.id)
                            .param("decision", "REJECTED")
                            .with(oidcLogin()
                                    .idToken(token -> token.claims(
                                            claim -> {
                                                claim.put(SilasConstants.USER_EMAIL, "test@example.com");
                                            }
                                    ))
                                    .authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"))))
                    .andExpect(status().isOk())
                    .andExpect(view().name("decision-reason"))
                    .andExpect(model().attribute("user", linkedRequest))
                    .andExpect(model().attribute("decision", "REJECTED"));
        }
    }

    @Nested
    @DisplayName("DecisionSuccessPage")
    class DecisionSuccessPage {
        @Test
        void shouldSubmitApprovalDecisionAndRedirectToSuccessPage() throws Exception {
            LinkedRequest linkedRequest = createMockLinkedRequests().get(0);
            when(linkedRequestService.getRequestById(anyString())).thenReturn(Optional.of(linkedRequest));

            mockMvc.perform(post("/internal/manage-linking-account/manage/{id}/submit-decision", linkedRequest.id)
                            .param("decision", "APPROVED")
                            .param("decisionReason", "Request meets all criteria"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/internal/manage-linking-account/decision-success/" + linkedRequest.id))
                    .andExpect(flash().attribute("decision", "APPROVED"))
                    .andExpect(flash().attribute("user", linkedRequest));

            verify(linkedRequestService).updateRequestDecision(eq(linkedRequest.id.toString()), eq("APPROVED"), eq("Request meets all criteria"));
        }

        @Test
        void shouldSubmitRejectionDecisionAndRedirectToSuccessPage() throws Exception {
            LinkedRequest linkedRequest = createMockLinkedRequests().get(0);
            when(linkedRequestService.getRequestById(anyString())).thenReturn(Optional.of(linkedRequest));

            mockMvc.perform(post("/internal/manage-linking-account/manage/{id}/submit-decision", linkedRequest.id)
                            .param("decision", "REJECTED")
                            .param("decisionReason", "Insufficient documentation provided"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/internal/manage-linking-account/decision-success/" + linkedRequest.id))
                    .andExpect(flash().attribute("decision", "REJECTED"))
                    .andExpect(flash().attribute("user", linkedRequest));

            verify(linkedRequestService).updateRequestDecision(eq(linkedRequest.id.toString()), eq("REJECTED"), eq("Insufficient documentation provided"));
        }

        @Test
        void shouldShowDecisionSuccessPageForApprovedRequest() throws Exception {
            LinkedRequest approvedRequest = createMockLinkedRequests().get(0).toBuilder()
                    .status(Status.APPROVED)
                    .build();
            when(linkedRequestService.getRequestById(anyString())).thenReturn(Optional.of(approvedRequest));

            mockMvc.perform(get("/internal/manage-linking-account/decision-success/{id}", approvedRequest.id))
                    .andExpect(status().isOk())
                    .andExpect(view().name("request-decision-success"))
                    .andExpect(model().attribute("user", approvedRequest))
                    .andExpect(model().attribute("decision", "APPROVED"));
        }

        @Test
        void shouldShowDecisionSuccessPageForRejectedRequest() throws Exception {
            LinkedRequest rejectedRequest = createMockLinkedRequests().get(0).toBuilder()
                    .status(Status.REJECTED)
                    .build();
            when(linkedRequestService.getRequestById(anyString())).thenReturn(Optional.of(rejectedRequest));

            mockMvc.perform(get("/internal/manage-linking-account/decision-success/{id}", rejectedRequest.id))
                    .andExpect(status().isOk())
                    .andExpect(view().name("request-decision-success"))
                    .andExpect(model().attribute("user", rejectedRequest))
                    .andExpect(model().attribute("decision", "REJECTED"));
        }
    }
}
