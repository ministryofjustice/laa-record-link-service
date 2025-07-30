package uk.gov.justice.record.link.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.thymeleaf.util.StringUtils;
import uk.gov.justice.record.link.entity.CcmsUser;
import uk.gov.justice.record.link.entity.LinkedRequest;
import uk.gov.justice.record.link.entity.Status;
import uk.gov.justice.record.link.model.UserTransferRequest;
import uk.gov.justice.record.link.respository.CcmsUserRepository;
import uk.gov.justice.record.link.respository.LinkedRequestRepository;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UserTransferServiceTest {

    private UserTransferService userTransferService;
    private CcmsUserRepository ccmsUserRepository;
    private LinkedRequestRepository linkedRequestRepository;

    @BeforeEach
    void setUp() {
        ccmsUserRepository = mock(CcmsUserRepository.class);
        linkedRequestRepository = mock(LinkedRequestRepository.class);
        userTransferService = new UserTransferService(linkedRequestRepository, ccmsUserRepository);
    }

    @Test
    void saveUserTransferRequest() {
        UserTransferRequest transferRequest = new UserTransferRequest();
        transferRequest.setOldLogin("Alice");
        transferRequest.setAdditionalInfo("My surname has changed due to marriage.");

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

        when(ccmsUserRepository.save(ccmsUser)).thenReturn(ccmsUser);
        when(linkedRequestRepository.save(linkedRequest)).thenReturn(linkedRequest);

        userTransferService.save(transferRequest);

        verify(ccmsUserRepository).save(any(CcmsUser.class));
        verify(ccmsUserRepository, times(1)).save(any(CcmsUser.class));
        verify(linkedRequestRepository).save(any(LinkedRequest.class));
        verify(linkedRequestRepository, times(1)).save(any(LinkedRequest.class));
    }
}
