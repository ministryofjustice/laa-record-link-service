package uk.gov.justice.record.link.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.record.link.entity.LinkedRequest;
import uk.gov.justice.record.link.entity.Status;
import uk.gov.justice.record.link.respository.LinkedRequestRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LinkedRequestService {

    private final LinkedRequestRepository linkedRequestRepository;

    public Page<LinkedRequest> getAllLinkingRequests(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Order.asc("createdDate")));

        return linkedRequestRepository.findAll(pageable);
    }

    public Page<LinkedRequest> getLinkingRequestByOldLogin(String oldLogin, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Order.asc("createdDate")));
        return linkedRequestRepository.findByOldLoginIdContainingAllIgnoreCase(oldLogin, pageable);

    }

    public Page<LinkedRequest> getAssignedRequests(String assignee, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Order.asc("createdDate")));
        return linkedRequestRepository.findByLaaAssignee(assignee, pageable);
    }

    public Optional<LinkedRequest> getRequestById(String id) {
        try {
            UUID uuid = UUID.fromString(id);
            return linkedRequestRepository.findById(uuid);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    @Transactional
    public Optional<LinkedRequest> assignNextCase(String assigneeEmail) {
        Optional<LinkedRequest> nextRequest = linkedRequestRepository
                .findFirstByLaaAssigneeIsNullAndStatusOrderByCreatedDateAsc(Status.OPEN);
        
        if (nextRequest.isPresent()) {
            LinkedRequest request = nextRequest.get();
            LinkedRequest updatedRequest = request.toBuilder()
                    .laaAssignee(assigneeEmail)
                    .assignedDate(LocalDateTime.now())
                    .build();
            
            return Optional.of(linkedRequestRepository.save(updatedRequest));
        }
        
        return Optional.empty();
    }

    @Transactional
    public void updateRequestDecision(String id, String decision, String decisionReason) {
        UUID uuid = UUID.fromString(id);
        Optional<LinkedRequest> requestOpt = linkedRequestRepository.findById(uuid);
        
        if (requestOpt.isPresent()) {
            LinkedRequest request = requestOpt.get();
            Status status = Status.valueOf(decision);
            
            LinkedRequest updatedRequest = request.toBuilder()
                    .status(status)
                    .decisionReason(decisionReason)
                    .decisionDate(LocalDateTime.now())
                    .build();
            
            linkedRequestRepository.save(updatedRequest);
        }
    }

}
