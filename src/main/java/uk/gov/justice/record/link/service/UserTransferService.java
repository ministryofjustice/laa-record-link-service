package uk.gov.justice.record.link.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.justice.record.link.constants.ValidationConstants;
import uk.gov.justice.record.link.entity.CcmsUser;
import uk.gov.justice.record.link.entity.LinkedRequest;
import uk.gov.justice.record.link.entity.Status;
import uk.gov.justice.record.link.model.UserTransferRequest;
import uk.gov.justice.record.link.respository.CcmsUserRepository;
import uk.gov.justice.record.link.respository.LinkedRequestRepository;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
@Slf4j
public class UserTransferService {

    private final LinkedRequestRepository linkedRequestRepository;
    private final CcmsUserRepository ccmsUserRepository;
    private static final String SYSTEM_USER = "System";


    public void createRequest(final UserTransferRequest userTransferRequest) {
        ccmsUserRepository.findByLoginId(userTransferRequest.getOldLogin()).ifPresent(ccmsUser -> {
            log.debug("Creating link request for user {}", userTransferRequest.getOldLogin());
            LinkedRequest.LinkedRequestBuilder<?, ?> initialLinkRequest = createBaseLinkRequestEntity(userTransferRequest, ccmsUser);
            LinkedRequest linkedRequest = isRequestAutoApproved(userTransferRequest, ccmsUser)
                    ? createApprovedRequestEntity(initialLinkRequest)
                    : createOpenRequestEntity(initialLinkRequest);
            linkedRequestRepository.save(linkedRequest);
            log.debug("Link request created for user with login id {} with status {}", userTransferRequest.getOldLogin(), linkedRequest.getStatus());
        });
    }

    public void rejectRequest(final UserTransferRequest userTransferRequest, final String reason) {
        CcmsUser ccmsUser = ccmsUserRepository.findByLoginId(userTransferRequest.getOldLogin()).orElse(null);
        LinkedRequest.LinkedRequestBuilder<?, ?> initialLinkRequest = createBaseLinkRequestEntity(userTransferRequest, ccmsUser);
        LinkedRequest linkedRequest = createRejectedRequestEntity(initialLinkRequest, reason);
        linkedRequestRepository.save(linkedRequest);
        log.error("Link request created for user with login id {} with status {}", userTransferRequest.getOldLogin(), Status.REJECTED);
    }

    public Page<LinkedRequest> getRequestsForCurrentUser(String userName, Pageable pageable) {
        return linkedRequestRepository.findByIdamLegacyUserId(userName, pageable);
    }

    private Boolean isRequestAutoApproved(final UserTransferRequest userTransferRequest, final CcmsUser ccmsUser) {
        return  StringUtils.startsWithIgnoreCase(ccmsUser.getFirstName(), userTransferRequest.getFirstName())
                && StringUtils.equalsIgnoreCase(userTransferRequest.getLastName(), ccmsUser.getLastName());
    }

    private LinkedRequest.LinkedRequestBuilder<?, ?> createBaseLinkRequestEntity(final UserTransferRequest userTransferRequest, final CcmsUser ccmsUser) {
        return LinkedRequest.builder()
                .ccmsUser(ccmsUser)
                .oldLoginId(userTransferRequest.getOldLogin())
                .idamLegacyUserId(userTransferRequest.getLegacyUserId())
                .idamFirstName(userTransferRequest.getFirstName())
                .idamLastName(userTransferRequest.getLastName())
                .idamFirmCode(userTransferRequest.getFirmCode())
                .idamFirmName(userTransferRequest.getFirmName())
                .idamEmail(userTransferRequest.getEmail())
                .additionalInfo(userTransferRequest.getAdditionalInfo())
                .createdDate(LocalDateTime.now());
    }

    private LinkedRequest createOpenRequestEntity(final LinkedRequest.LinkedRequestBuilder<?, ?> initialLinkRequest) {
        return initialLinkRequest
                .status(Status.OPEN)
                .build();
    }

    private LinkedRequest createApprovedRequestEntity(final LinkedRequest.LinkedRequestBuilder<?, ?> initialLinkRequest) {
        return initialLinkRequest
                .status(Status.APPROVED)
                .decisionDate(LocalDateTime.now())
                .decisionReason(ValidationConstants.AUTO_APPROVED_MESSAGE)
                .laaAssignee(SYSTEM_USER)
                .build();
    }

    private LinkedRequest createRejectedRequestEntity(final LinkedRequest.LinkedRequestBuilder<?, ?> initialLinkRequest, final String reason) {
        return initialLinkRequest
                .status(Status.REJECTED)
                .decisionDate(LocalDateTime.now())
                .decisionReason(reason)
                .laaAssignee(SYSTEM_USER)
                .build();
    }
}
