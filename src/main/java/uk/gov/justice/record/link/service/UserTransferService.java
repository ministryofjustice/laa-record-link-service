package uk.gov.justice.record.link.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;
import uk.gov.justice.record.link.entity.CcmsUser;
import uk.gov.justice.record.link.entity.LinkedRequest;
import uk.gov.justice.record.link.entity.Status;
import uk.gov.justice.record.link.model.UserTransferRequest;
import uk.gov.justice.record.link.respository.CcmsUserRepository;
import uk.gov.justice.record.link.respository.LinkedRequestRepository;

import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class UserTransferService {

    private final LinkedRequestRepository linkedRequestRepository;
    private final CcmsUserRepository ccmsUserRepository;

    public void save(final UserTransferRequest userTransferRequest) {

        CcmsUser ccmsUser = ccmsUserRepository.findByLoginId(userTransferRequest.getOldLogin());

        // TODO Revisit in STB-2368
        LinkedRequest newUser = LinkedRequest.builder()
                .additionalInfo(userTransferRequest.getAdditionalInfo())
                .ccmsUser(ccmsUser)
                .status(Status.OPEN)
                .idamFirstName("TODO in STB-2368")
                .idamLastName("TODO in STB-2368")
                .idamLegacyUserId(UUID.randomUUID())
                .idamEmail(StringUtils.randomAlphanumeric(6))
                .createdDate(LocalDateTime.now())
                .build();
        linkedRequestRepository.save(newUser);
    }
}