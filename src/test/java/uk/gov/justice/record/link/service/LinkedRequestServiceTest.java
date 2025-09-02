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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    private ArgumentCaptor<Pageable> pegeableCaptor;

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

            verify(linkedRequestRepository).findByOldLoginIdContainingAllIgnoreCase(oldLoginCaptor.capture(), pegeableCaptor.capture());

            assertThat(oldLoginCaptor.getValue()).isEqualTo("oldLoginId");

            assertThat(pegeableCaptor.getValue().getPageNumber()).isEqualTo(0);
            assertThat(pegeableCaptor.getValue().getSort()).isEqualTo(Sort.by(Sort.Order.asc("createdDate")));
            assertThat(pegeableCaptor.getValue().getPageSize()).isEqualTo(10);
        }

    }
}
