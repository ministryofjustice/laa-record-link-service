package uk.gov.justice.record.link.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import uk.gov.justice.record.link.entity.CcmsUser;
import uk.gov.justice.record.link.entity.LinkedRequest;
import uk.gov.justice.record.link.entity.Status;
import uk.gov.justice.record.link.respository.LinkedRequestRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.any;

@ExtendWith(MockitoExtension.class)
class LinkedRequestServiceTest {

    @Mock
    private LinkedRequestRepository linkedRequestRepository;

    @InjectMocks
    private LinkedRequestService linkedRequestService;

    private List<LinkedRequest> mockLinkedRequests;
    private Page<LinkedRequest> mockPage;

    @Captor
    private ArgumentCaptor<String> oldLoginCaptor;
    @Captor
    private ArgumentCaptor<Pageable> pageableCaptor;

    @Nested
    @DisplayName("ShouldReturnPagedResults")
    class GetAllLinkingRequests {

        @BeforeEach
        void setUp() {
            mockLinkedRequests = createMockLinkedRequests();
            mockPage = new PageImpl<>(mockLinkedRequests, PageRequest.of(0, 10), 25);
        }

        @Test
        void getAllLinkingRequestsCalled() {
            int page = 1;
            int size = 10;
            when(linkedRequestRepository.findAll(any(Pageable.class))).thenReturn(mockPage);

            Page<LinkedRequest> result = linkedRequestService.getAllLinkingRequests(page, size);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(3);
            assertThat(result.getTotalElements()).isEqualTo(25);
            assertThat(result.getTotalPages()).isEqualTo(3);
            assertThat(result.getNumber()).isEqualTo(0); // Spring Data uses 0-based indexing
            assertThat(result.getSize()).isEqualTo(10);
        }

        @Test
        void calledWithPageAndSize() {
            int page = 2;
            int size = 5;
            when(linkedRequestRepository.findAll(any(Pageable.class))).thenReturn(mockPage);

            linkedRequestService.getAllLinkingRequests(page, size);

            Pageable expectedPageable = PageRequest.of(1, 5, Sort.by(Sort.Order.asc("createdDate")));
            verify(linkedRequestRepository).findAll(expectedPageable);
        }

        @Test
        void calledWithOneBasedPageNumber() {
            int page = 3;
            int size = 15;
            when(linkedRequestRepository.findAll(any(Pageable.class))).thenReturn(mockPage);

            linkedRequestService.getAllLinkingRequests(page, size);

            Pageable expectedPageable = PageRequest.of(2, 15, Sort.by(Sort.Order.asc("createdDate")));
            verify(linkedRequestRepository).findAll(expectedPageable);
        }

        @Test
        void calledWithPageOne() {
            int page = 1;
            int size = 10;
            when(linkedRequestRepository.findAll(any(Pageable.class))).thenReturn(mockPage);

            linkedRequestService.getAllLinkingRequests(page, size);

            Pageable expectedPageable = PageRequest.of(0, 10, Sort.by(Sort.Order.asc("createdDate")));
            verify(linkedRequestRepository).findAll(expectedPageable);
        }

        @Test
        void noDataExists() {
            int page = 1;
            int size = 10;
            Page<LinkedRequest> emptyPage = new PageImpl<>(Arrays.asList(), PageRequest.of(0, 10), 0);
            when(linkedRequestRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

            Page<LinkedRequest> result = linkedRequestService.getAllLinkingRequests(page, size);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);
            assertThat(result.getTotalPages()).isEqualTo(0);
            assertThat(result.hasContent()).isFalse();
        }

        @Test
        void calledWithAnyParameters() {
            int page = 1;
            int size = 10;
            when(linkedRequestRepository.findAll(any(Pageable.class))).thenReturn(mockPage);

            linkedRequestService.getAllLinkingRequests(page, size);

            Pageable expectedPageable = PageRequest.of(0, 10, Sort.by(Sort.Order.asc("createdDate")));
            verify(linkedRequestRepository).findAll(expectedPageable);
        }

        @Test
        void calledWithSecondPage() {
            int page = 2;
            int size = 10;
            Page<LinkedRequest> secondPage = new PageImpl<>(mockLinkedRequests, PageRequest.of(1, 10), 25);
            when(linkedRequestRepository.findAll(any(Pageable.class))).thenReturn(secondPage);

            Page<LinkedRequest> result = linkedRequestService.getAllLinkingRequests(page, size);

            assertThat(result.getNumber()).isEqualTo(1);
            assertThat(result.getSize()).isEqualTo(10);
            assertThat(result.getTotalElements()).isEqualTo(25);
            assertThat(result.getTotalPages()).isEqualTo(3);
            assertThat(result.hasPrevious()).isTrue();
            assertThat(result.hasNext()).isTrue();
            assertThat(result.isFirst()).isFalse();
            assertThat(result.isLast()).isFalse();
        }

        @Test
        void calledWithFinalPageNumber() {
            int page = 3;
            int size = 10;
            Page<LinkedRequest> lastPage = new PageImpl<>(mockLinkedRequests, PageRequest.of(2, 10), 25);
            when(linkedRequestRepository.findAll(any(Pageable.class))).thenReturn(lastPage);

            Page<LinkedRequest> result = linkedRequestService.getAllLinkingRequests(page, size);

            assertThat(result.getNumber()).isEqualTo(2);
            assertThat(result.hasPrevious()).isTrue();
            assertThat(result.hasNext()).isFalse();
            assertThat(result.isFirst()).isFalse();
            assertThat(result.isLast()).isTrue();
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
    }

    @Nested
    class GetLinkingRequestByOldLogin {

        @DisplayName("Should call findOldLogin with right param")
        @Test
        void shouldCallFindOldLoginWithRightParam() {

            linkedRequestService.getLinkingRequestByOldLogin("oldLoginId", 1, 10);

            verify(linkedRequestRepository).findByOldLoginIdContainingAllIgnoreCase(oldLoginCaptor.capture(), pageableCaptor.capture());

            assertThat(oldLoginCaptor.getValue()).isEqualTo("oldLoginId");

            Pageable capturedPageable = pageableCaptor.getValue();
            assertThat(capturedPageable.getPageNumber()).isEqualTo(0);
            assertThat(capturedPageable.getSort()).isEqualTo(Sort.by(Sort.Order.asc("createdDate")));
            assertThat(capturedPageable.getPageSize()).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("Should return request when valid UUID is provided")
    class GetRequestById {

        @Test
        void shouldGetRequestById() {

            UUID validUuid = UUID.randomUUID();
            String validUuidStr = validUuid.toString();
            LinkedRequest mockRequest = new LinkedRequest();
            when(linkedRequestRepository.findById(validUuid)).thenReturn(Optional.of(mockRequest));

            Optional<LinkedRequest> result = linkedRequestService.getRequestById(validUuidStr);

            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(mockRequest);

            verify(linkedRequestRepository).findById(validUuid);
        }

        @Test
        void shouldReturnEmptyWhenInvalidUuid() {

            String invalidUuidStr = "not-a-valid-uuid";

            Optional<LinkedRequest> result = linkedRequestService.getRequestById(invalidUuidStr);

            assertThat(result).isEmpty();

            verify(linkedRequestRepository, never()).findById(any());
        }
    }

    @Nested
    @DisplayName("GetAssignedRequests")
    class GetAssignedRequests {

        @Captor
        private ArgumentCaptor<String> assigneeCaptor;

        @Test
        void shouldReturnPagedAssignedRequests() {
            List<LinkedRequest> mockAssignedRequests = createMockAssignedRequests();
            Page<LinkedRequest> mockAssignedPage = new PageImpl<>(mockAssignedRequests, PageRequest.of(0, 10), 15);
            
            when(linkedRequestRepository.findByLaaAssignee(any(String.class), any(Pageable.class))).thenReturn(mockAssignedPage);

            Page<LinkedRequest> result = linkedRequestService.getAssignedRequests("testUser", 1, 10);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(15);
            assertThat(result.getTotalPages()).isEqualTo(2);
            assertThat(result.getNumber()).isEqualTo(0);
            assertThat(result.getSize()).isEqualTo(10);
        }

        @Test
        void shouldCallRepositoryWithCorrectParameters() {
            String assignee = "testUser";
            int page = 2;
            int size = 5;
            
            Page<LinkedRequest> mockPage = new PageImpl<>(List.of(), PageRequest.of(1, 5), 0);
            when(linkedRequestRepository.findByLaaAssignee(any(String.class), any(Pageable.class))).thenReturn(mockPage);

            linkedRequestService.getAssignedRequests(assignee, page, size);

            verify(linkedRequestRepository).findByLaaAssignee(assigneeCaptor.capture(), pageableCaptor.capture());
            
            assertThat(assigneeCaptor.getValue()).isEqualTo("testUser");
            
            Pageable capturedPageable = pageableCaptor.getValue();
            assertThat(capturedPageable.getPageNumber()).isEqualTo(1);
            assertThat(capturedPageable.getPageSize()).isEqualTo(5);
            assertThat(capturedPageable.getSort()).isEqualTo(Sort.by(Sort.Order.asc("createdDate")));
        }

        @Test
        void shouldHandleEmptyResults() {
            Page<LinkedRequest> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
            when(linkedRequestRepository.findByLaaAssignee(any(String.class), any(Pageable.class))).thenReturn(emptyPage);

            Page<LinkedRequest> result = linkedRequestService.getAssignedRequests("testUser", 1, 10);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);
            assertThat(result.getTotalPages()).isEqualTo(0);
            assertThat(result.hasContent()).isFalse();
        }

        @Test
        void shouldConvertOneBasedPageToZeroBased() {
            Page<LinkedRequest> mockPage = new PageImpl<>(List.of(), PageRequest.of(2, 15), 0);
            when(linkedRequestRepository.findByLaaAssignee(any(String.class), any(Pageable.class))).thenReturn(mockPage);

            linkedRequestService.getAssignedRequests("testUser", 3, 15);

            verify(linkedRequestRepository).findByLaaAssignee(assigneeCaptor.capture(), pageableCaptor.capture());
            
            Pageable capturedPageable = pageableCaptor.getValue();
            assertThat(capturedPageable.getPageNumber()).isEqualTo(2);
            assertThat(capturedPageable.getPageSize()).isEqualTo(15);
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
                    .laaAssignee("testUser")
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
                    .laaAssignee("testUser")
                    .status(Status.APPROVED)
                    .build();

            return Arrays.asList(assignedRequest1, assignedRequest2);
        }
    }

    @Nested
    @DisplayName("AssignNextCase")
    class AssignNextCase {

        @Captor
        private ArgumentCaptor<LinkedRequest> savedRequestCaptor;

        @Test
        void shouldAssignNextCaseWhenUnassignedRequestExists() {
            String assigneeEmail = "test.user@example.com";
            LinkedRequest unassignedRequest = createUnassignedRequest();
            LinkedRequest assignedRequest = unassignedRequest.toBuilder()
                    .laaAssignee(assigneeEmail)
                    .assignedDate(LocalDateTime.now())
                    .build();

            when(linkedRequestRepository.findFirstByLaaAssigneeIsNullAndStatusOrderByCreatedDateAsc(Status.OPEN))
                    .thenReturn(Optional.of(unassignedRequest));
            when(linkedRequestRepository.save(any(LinkedRequest.class)))
                    .thenReturn(assignedRequest);

            Optional<LinkedRequest> result = linkedRequestService.assignNextCase(assigneeEmail);

            assertThat(result).isPresent();
            assertThat(result.get().getLaaAssignee()).isEqualTo(assigneeEmail);
            assertThat(result.get().getAssignedDate()).isNotNull();

            verify(linkedRequestRepository).findFirstByLaaAssigneeIsNullAndStatusOrderByCreatedDateAsc(Status.OPEN);
            verify(linkedRequestRepository).save(savedRequestCaptor.capture());

            LinkedRequest savedRequest = savedRequestCaptor.getValue();
            assertThat(savedRequest.getLaaAssignee()).isEqualTo(assigneeEmail);
            assertThat(savedRequest.getAssignedDate()).isNotNull();
        }

        @Test
        void shouldReturnEmptyWhenNoUnassignedRequestExists() {
            String assigneeEmail = "test.user@example.com";
            when(linkedRequestRepository.findFirstByLaaAssigneeIsNullAndStatusOrderByCreatedDateAsc(Status.OPEN))
                    .thenReturn(Optional.empty());

            Optional<LinkedRequest> result = linkedRequestService.assignNextCase(assigneeEmail);

            assertThat(result).isEmpty();

            verify(linkedRequestRepository).findFirstByLaaAssigneeIsNullAndStatusOrderByCreatedDateAsc(Status.OPEN);
            verify(linkedRequestRepository, org.mockito.Mockito.never()).save(any(LinkedRequest.class));
        }

        @Test
        void shouldPreserveOriginalRequestDataWhenAssigning() {
            // Given
            String assigneeEmail = "test.user@example.com";
            LinkedRequest originalRequest = createUnassignedRequest();
            LinkedRequest assignedRequest = originalRequest.toBuilder()
                    .laaAssignee(assigneeEmail)
                    .assignedDate(LocalDateTime.now())
                    .build();

            when(linkedRequestRepository.findFirstByLaaAssigneeIsNullAndStatusOrderByCreatedDateAsc(Status.OPEN))
                    .thenReturn(Optional.of(originalRequest));
            when(linkedRequestRepository.save(any(LinkedRequest.class)))
                    .thenReturn(assignedRequest);

            Optional<LinkedRequest> result = linkedRequestService.assignNextCase(assigneeEmail);

            assertThat(result).isPresent();
            LinkedRequest resultRequest = result.get();

            assertThat(resultRequest.getIdamFirstName()).isEqualTo(originalRequest.getIdamFirstName());
            assertThat(resultRequest.getIdamLastName()).isEqualTo(originalRequest.getIdamLastName());
            assertThat(resultRequest.getIdamEmail()).isEqualTo(originalRequest.getIdamEmail());
            assertThat(resultRequest.getOldLoginId()).isEqualTo(originalRequest.getOldLoginId());
            assertThat(resultRequest.getStatus()).isEqualTo(originalRequest.getStatus());
            assertThat(resultRequest.getCreatedDate()).isEqualTo(originalRequest.getCreatedDate());

            assertThat(resultRequest.getLaaAssignee()).isEqualTo(assigneeEmail);
            assertThat(resultRequest.getAssignedDate()).isNotNull();
        }

        private LinkedRequest createUnassignedRequest() {
            return LinkedRequest.builder()
                    .id(UUID.randomUUID())
                    .oldLoginId("unassigned_user")
                    .idamLegacyUserId(UUID.randomUUID().toString())
                    .idamFirstName("Unassigned")
                    .idamLastName("User")
                    .idamFirmName("Unassigned Firm")
                    .idamFirmCode("UF001")
                    .idamEmail("unassigned.user@example.com")
                    .createdDate(LocalDateTime.now().minusDays(1))
                    .status(Status.OPEN)
                    .laaAssignee(null)
                    .assignedDate(null)
                    .build();
        }
    }
}
