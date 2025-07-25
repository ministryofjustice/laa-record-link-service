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
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        ReflectionTestUtils.setField(demoDataPopulator, "internalUserPrincipals", Collections.emptySet());
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


    @Test
    void shouldCreateInternalUsers_whenInternalUserPrincipalsProvided() {
        Set<String> internalUsers = Set.of(
                "user1:John:Doe:Firm1:F001:john@firm1.com",
                "user2:Jane:Smith:Firm2:F002:jane@firm2.com"
        );
        ReflectionTestUtils.setField(demoDataPopulator, "internalUserPrincipals", internalUsers);

        when(ccmsUserRepository.findCcmsUserByLoginId("jsmith001")).thenReturn(Collections.emptyList());
        when(ccmsUserRepository.findCcmsUserByLoginId("user1")).thenReturn(Collections.emptyList());
        when(ccmsUserRepository.findCcmsUserByLoginId("user2")).thenReturn(Collections.emptyList());
        when(ccmsUserRepository.save(any(CcmsUser.class))).thenReturn(createMockCcmsUser());
        when(linkedRequestRepository.save(any(LinkedRequest.class))).thenReturn(createMockLinkedRequest());

        demoDataPopulator.appReady(applicationReadyEvent);

        verify(ccmsUserRepository, times(5)).save(any(CcmsUser.class));
        verify(linkedRequestRepository, atLeast(13)).save(any(LinkedRequest.class));
    }

    @Test
    void shouldNotCreateInternalUsers_whenInternalUserAlreadyExists() {
        Set<String> internalUsers = Set.of("user1:John:Doe:Firm1:F001:john@firm1.com");
        ReflectionTestUtils.setField(demoDataPopulator, "internalUserPrincipals", internalUsers);

        when(ccmsUserRepository.findCcmsUserByLoginId("jsmith001")).thenReturn(Collections.emptyList());
        when(ccmsUserRepository.findCcmsUserByLoginId("user1")).thenReturn(List.of(createMockCcmsUser()));
        when(ccmsUserRepository.save(any(CcmsUser.class))).thenReturn(createMockCcmsUser());
        when(linkedRequestRepository.save(any(LinkedRequest.class))).thenReturn(createMockLinkedRequest());

        demoDataPopulator.appReady(applicationReadyEvent);

        verify(ccmsUserRepository, times(3)).save(any(CcmsUser.class));
        verify(linkedRequestRepository, atLeast(7)).save(any(LinkedRequest.class));
    }

    @Test
    void shouldIgnoreInvalidInternalUserFormat() {
        Set<String> internalUsers = Set.of(
                "invalidformat",
                "user1:only:three:parts",
                "user2:John:Doe:Firm1:F001:john@firm1.com"
        );
        ReflectionTestUtils.setField(demoDataPopulator, "internalUserPrincipals", internalUsers);

        when(ccmsUserRepository.findCcmsUserByLoginId("jsmith001")).thenReturn(List.of(createMockCcmsUser()));
        when(ccmsUserRepository.findCcmsUserByLoginId("user2")).thenReturn(Collections.emptyList());
        when(ccmsUserRepository.save(any(CcmsUser.class))).thenReturn(createMockCcmsUser());

        demoDataPopulator.appReady(applicationReadyEvent);

        verify(ccmsUserRepository, times(1)).save(any(CcmsUser.class));
        verify(linkedRequestRepository, atLeast(3)).save(any(LinkedRequest.class));
    }

    @Test
    void shouldHandleEmptyInternalUserPrincipals() {
        ReflectionTestUtils.setField(demoDataPopulator, "internalUserPrincipals", Collections.emptySet());

        when(ccmsUserRepository.findCcmsUserByLoginId("jsmith001")).thenReturn(Collections.emptyList());
        when(ccmsUserRepository.save(any(CcmsUser.class))).thenReturn(createMockCcmsUser());
        when(linkedRequestRepository.save(any(LinkedRequest.class))).thenReturn(createMockLinkedRequest());

        demoDataPopulator.appReady(applicationReadyEvent);

        verify(ccmsUserRepository, times(3)).save(any(CcmsUser.class));
        verify(linkedRequestRepository, atLeast(7)).save(any(LinkedRequest.class));
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
