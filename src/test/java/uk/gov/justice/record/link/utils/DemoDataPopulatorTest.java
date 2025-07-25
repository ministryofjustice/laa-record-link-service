package uk.gov.justice.record.link.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.justice.record.link.entity.CcmsUser;
import uk.gov.justice.record.link.entity.LinkedRequest;
import uk.gov.justice.record.link.entity.Status;
import uk.gov.justice.record.link.respository.CcmsUserRepository;
import uk.gov.justice.record.link.respository.LinkedRequestRepository;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DemoDataPopulatorTest {

    @Mock
    private CcmsUserRepository ccmsUserRepository;

    @Mock
    private LinkedRequestRepository linkedRequestRepository;

    @Mock
    private ApplicationReadyEvent applicationReadyEvent;

    @InjectMocks
    private DemoDataPopulator demoDataPopulator;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(demoDataPopulator, "populateDummyData", true);
    }

    @Test
    void shouldCallPopulateDummyData_whenPopulateDummyDataIsTrue_() {
        when(ccmsUserRepository.findCcmsUserByLoginId("jsmith001")).thenReturn(Collections.emptyList());
        when(ccmsUserRepository.save(any(CcmsUser.class))).thenReturn(createMockCcmsUser());
        when(linkedRequestRepository.save(any(LinkedRequest.class))).thenReturn(createMockLinkedRequest());

        demoDataPopulator.appReady(applicationReadyEvent);

        verify(ccmsUserRepository).findCcmsUserByLoginId("jsmith001");
        verify(ccmsUserRepository, times(3)).save(any(CcmsUser.class));
        verify(linkedRequestRepository, atLeast(7)).save(any(LinkedRequest.class));
    }

    @Test
    void shouldNotCallPopulateDummyData_whenPopulateDummyDataIsFalse_() {
        ReflectionTestUtils.setField(demoDataPopulator, "populateDummyData", false);

        demoDataPopulator.appReady(applicationReadyEvent);

        verify(ccmsUserRepository, never()).findCcmsUserByLoginId(anyString());
        verify(ccmsUserRepository, never()).save(any(CcmsUser.class));
        verify(linkedRequestRepository, never()).save(any(LinkedRequest.class));
    }

    @Test
    void shouldNotCreateDummyData_whenUserAlreadyExists() {
        CcmsUser existingUser = createMockCcmsUser();
        when(ccmsUserRepository.findCcmsUserByLoginId("jsmith001")).thenReturn(List.of(existingUser));

        demoDataPopulator.appReady(applicationReadyEvent);

        verify(ccmsUserRepository).findCcmsUserByLoginId("jsmith001");
        verify(ccmsUserRepository, never()).save(any(CcmsUser.class));
        verify(linkedRequestRepository, never()).save(any(LinkedRequest.class));
    }

    @Test
    void shouldCreateAllDummyData_whenNoExistingUsers() {
        when(ccmsUserRepository.findCcmsUserByLoginId("jsmith001")).thenReturn(Collections.emptyList());
        when(ccmsUserRepository.save(any(CcmsUser.class))).thenReturn(createMockCcmsUser());
        when(linkedRequestRepository.save(any(LinkedRequest.class))).thenReturn(createMockLinkedRequest());

        demoDataPopulator.appReady(applicationReadyEvent);

        verify(ccmsUserRepository, times(3)).save(any(CcmsUser.class));
        verify(linkedRequestRepository, atLeast(7)).save(any(LinkedRequest.class));
    }

    @Test
    void shouldCatchException() {
        when(ccmsUserRepository.findCcmsUserByLoginId("jsmith001")).thenThrow(new RuntimeException("Database error"));

        demoDataPopulator.appReady(applicationReadyEvent);

        verify(ccmsUserRepository).findCcmsUserByLoginId("jsmith001");
        verify(ccmsUserRepository, never()).save(any(CcmsUser.class));
    }

    @Test
    void shouldCreateUserAndTwoRequests() {
        when(ccmsUserRepository.findCcmsUserByLoginId("jsmith001")).thenReturn(Collections.emptyList());
        CcmsUser mockUser = createMockCcmsUser();
        when(ccmsUserRepository.save(any(CcmsUser.class))).thenReturn(mockUser);
        when(linkedRequestRepository.save(any(LinkedRequest.class))).thenReturn(createMockLinkedRequest());

        demoDataPopulator.appReady(applicationReadyEvent);

        verify(ccmsUserRepository, times(3)).save(any(CcmsUser.class));
        verify(linkedRequestRepository, atLeast(2)).save(any(LinkedRequest.class));
    }

    @Test
    void shouldCreateUserAndThreeRequests() {
        when(ccmsUserRepository.findCcmsUserByLoginId("jsmith001")).thenReturn(Collections.emptyList());
        CcmsUser mockUser = createMockCcmsUser();
        when(ccmsUserRepository.save(any(CcmsUser.class))).thenReturn(mockUser);
        when(linkedRequestRepository.save(any(LinkedRequest.class))).thenReturn(createMockLinkedRequest());

        demoDataPopulator.appReady(applicationReadyEvent);

        verify(ccmsUserRepository, times(3)).save(any(CcmsUser.class));
        verify(linkedRequestRepository, atLeast(3)).save(any(LinkedRequest.class));
    }

    @Test
    void shouldCreateUserAndTwoRequestsWithAssignment() {
        when(ccmsUserRepository.findCcmsUserByLoginId("jsmith001")).thenReturn(Collections.emptyList());
        CcmsUser mockUser = createMockCcmsUser();
        when(ccmsUserRepository.save(any(CcmsUser.class))).thenReturn(mockUser);
        LinkedRequest mockRequest = createMockLinkedRequest();
        when(linkedRequestRepository.save(any(LinkedRequest.class))).thenReturn(mockRequest);

        demoDataPopulator.appReady(applicationReadyEvent);

        verify(ccmsUserRepository, times(3)).save(any(CcmsUser.class));
        verify(linkedRequestRepository, atLeast(2)).save(any(LinkedRequest.class));
    }

    private CcmsUser createMockCcmsUser() {
        return CcmsUser.builder()
                .loginId("testuser")
                .firstName("Test")
                .lastName("User")
                .firmName("Test Firm")
                .firmCode("TF001")
                .email("test@example.com")
                .build();
    }

    private LinkedRequest createMockLinkedRequest() {
        return LinkedRequest.builder()
                .ccmsUser(createMockCcmsUser())
                .idamLegacyUserId(UUID.randomUUID())
                .idamFirstName("Test")
                .idamLastName("User")
                .idamEmail("test@example.com")
                .status(Status.OPEN)
                .build();
    }
}
