package uk.gov.justice.record.link.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.justice.record.link.entity.CcmsUser;
import uk.gov.justice.record.link.entity.LinkedRequest;
import uk.gov.justice.record.link.entity.Status;
import uk.gov.justice.record.link.model.UserTransferRequest;
import uk.gov.justice.record.link.respository.CcmsUserRepository;
import uk.gov.justice.record.link.respository.LinkedRequestRepository;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class UserTransferService {

    private final LinkedRequestRepository linkedRequestRepository;
    private final CcmsUserRepository ccmsUserRepository;
    private final CurrentUserService currentUserService;


    public void save(final UserTransferRequest userTransferRequest) {

        CcmsUser ccmsUser = ccmsUserRepository.findByLoginId(userTransferRequest.getOldLogin()).get();
        OidcTokenClaimsExtractor token = currentUserService.getCurrentUserClaims();

        LinkedRequest newUser = LinkedRequest.builder()
                .additionalInfo(userTransferRequest.getAdditionalInfo())
                .ccmsUser(ccmsUser)
                .oldLoginId(userTransferRequest.getOldLogin())
                .status(Status.OPEN)
                .idamFirstName(token.getFirstName())
                .idamLastName(token.getLastName())
                .idamLegacyUserId(token.getUserName())
                .idamEmail(token.getEmail())
                .createdDate(LocalDateTime.now())
                .build();
        linkedRequestRepository.save(newUser);

    }

    public void rejectRequest(final UserTransferRequest userTransferRequest, final String reason) {
        
        CcmsUser ccmsUser = ccmsUserRepository.findByLoginId(userTransferRequest.getOldLogin()).orElse(null);
        OidcTokenClaimsExtractor token = currentUserService.getCurrentUserClaims();

        LinkedRequest newUser = LinkedRequest.builder()
                .additionalInfo(userTransferRequest.getAdditionalInfo())
                .ccmsUser(ccmsUser)
                .oldLoginId(userTransferRequest.getOldLogin())
                .status(Status.REJECTED)
                .decisionDate(LocalDateTime.now())
                .decisionReason(reason)
                .laaAssignee("System")
                .idamFirstName(token.getFirstName())
                .idamLastName(token.getLastName())
                .idamLegacyUserId(token.getUserName())
                .idamEmail(token.getEmail())
                .createdDate(LocalDateTime.now())
                .build();
        linkedRequestRepository.save(newUser);
    }

    public Page<LinkedRequest> getRequestsForCurrentUser(String userName, Pageable pageable) {
    return linkedRequestRepository.findByIdamLegacyUserId(userName, pageable);
    }

}