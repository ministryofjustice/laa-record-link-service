package uk.gov.justice.record.link.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.ui.ConcurrentModel;
import uk.gov.justice.record.link.entity.CcmsUser;
import uk.gov.justice.record.link.entity.LinkedRequest;
import uk.gov.justice.record.link.entity.Status;
import uk.gov.justice.record.link.service.LinkedRequestService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ManageLinkingAccountRequestsControllerTest {

    @Mock
    private LinkedRequestService linkedRequestService;

    @InjectMocks
    private ManageLinkingAccountRequestsController controller;

    @Test
    void shouldReturnCorrectViewWithPaginationData_whenCalledWithDefaultParameters() {
        // Given
        List<LinkedRequest> mockRequests = createMockLinkedRequests();
        Page<LinkedRequest> mockPage = new PageImpl<>(mockRequests, PageRequest.of(0, 10), 15);
        
        when(linkedRequestService.getAllLinkingRequests(1, 10)).thenReturn(mockPage);

        ConcurrentModel model = new ConcurrentModel();

        String view = controller.manageRequests(1, 10, model);

        verify(linkedRequestService).getAllLinkingRequests(1, 10);
        assertThat(view).isEqualTo("manage-link-account-requests");
        assertThat(model.getAttribute("linkedRequests")).isNotNull();
        assertThat(model.getAttribute("currentPage")).isEqualTo(1);
        assertThat(model.getAttribute("totalPages")).isEqualTo(2);
        assertThat(model.getAttribute("totalItems")).isEqualTo(15L);
        assertThat(model.getAttribute("pageSize")).isEqualTo(10);
        assertThat(model.getAttribute("hasNext")).isEqualTo(true);
        assertThat(model.getAttribute("hasPrevious")).isEqualTo(false);
    }

    @Test
    void shouldHandleCustomPaginationParameters_whenCalledWithSpecificPageAndSize() {
        List<LinkedRequest> mockRequests = createMockLinkedRequests();
        Page<LinkedRequest> mockPage = new PageImpl<>(mockRequests, PageRequest.of(1, 5), 25);
        
        when(linkedRequestService.getAllLinkingRequests(2, 5)).thenReturn(mockPage);

        ConcurrentModel model = new ConcurrentModel();

        String view = controller.manageRequests(2, 5, model);

        verify(linkedRequestService).getAllLinkingRequests(2, 5);
        assertThat(view).isEqualTo("manage-link-account-requests");
        assertThat(model.getAttribute("currentPage")).isEqualTo(2);
        assertThat(model.getAttribute("totalPages")).isEqualTo(5);
        assertThat(model.getAttribute("totalItems")).isEqualTo(25L);
        assertThat(model.getAttribute("pageSize")).isEqualTo(5);
        assertThat(model.getAttribute("hasPrevious")).isEqualTo(true);
        assertThat(model.getAttribute("hasNext")).isEqualTo(true);
    }

    @Test
    void shouldHandleEmptyResults_whenNoLinkedRequestsExist() {
        Page<LinkedRequest> emptyPage = new PageImpl<>(Arrays.asList(), PageRequest.of(0, 10), 0);
        
        when(linkedRequestService.getAllLinkingRequests(1, 10)).thenReturn(emptyPage);

        ConcurrentModel model = new ConcurrentModel();

        String view = controller.manageRequests(1, 10, model);

        assertThat(view).isEqualTo("manage-link-account-requests");
        assertThat(model.getAttribute("linkedRequests")).isNotNull();
        assertThat(model.getAttribute("currentPage")).isEqualTo(1);
        assertThat(model.getAttribute("totalPages")).isEqualTo(0);
        assertThat(model.getAttribute("totalItems")).isEqualTo(0L);
        assertThat(model.getAttribute("pageSize")).isEqualTo(10);
        assertThat(model.getAttribute("hasPrevious")).isEqualTo(false);
        assertThat(model.getAttribute("hasNext")).isEqualTo(false);
    }

    @Test
    void shouldHandleLastPage_whenRequestingFinalPageOfResults() {
        List<LinkedRequest> mockRequests = createMockLinkedRequests();
        Page<LinkedRequest> lastPage = new PageImpl<>(mockRequests, PageRequest.of(2, 10), 23);
        
        when(linkedRequestService.getAllLinkingRequests(3, 10)).thenReturn(lastPage);

        ConcurrentModel model = new ConcurrentModel();

        String view = controller.manageRequests(3, 10, model);

        verify(linkedRequestService).getAllLinkingRequests(3, 10);
        assertThat(view).isEqualTo("manage-link-account-requests");
        assertThat(model.getAttribute("currentPage")).isEqualTo(3);
        assertThat(model.getAttribute("totalPages")).isEqualTo(3);
        assertThat(model.getAttribute("totalItems")).isEqualTo(23L);
        assertThat(model.getAttribute("hasPrevious")).isEqualTo(true);
        assertThat(model.getAttribute("hasNext")).isEqualTo(false);
    }

    @Test
    void shouldAddAllRequiredModelAttributes_whenCalledSuccessfully() {
        List<LinkedRequest> mockRequests = createMockLinkedRequests();
        Page<LinkedRequest> mockPage = new PageImpl<>(mockRequests, PageRequest.of(0, 10), 15);
        
        when(linkedRequestService.getAllLinkingRequests(1, 10)).thenReturn(mockPage);

        ConcurrentModel model = new ConcurrentModel();

        String view = controller.manageRequests(1, 10, model);

        assertThat(view).isEqualTo("manage-link-account-requests");
        assertThat(model.getAttribute("linkedRequests")).isNotNull();
        assertThat(model.getAttribute("currentPage")).isNotNull();
        assertThat(model.getAttribute("totalPages")).isNotNull();
        assertThat(model.getAttribute("totalItems")).isNotNull();
        assertThat(model.getAttribute("pageSize")).isNotNull();
        assertThat(model.getAttribute("hasPrevious")).isNotNull();
        assertThat(model.getAttribute("hasNext")).isNotNull();
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
