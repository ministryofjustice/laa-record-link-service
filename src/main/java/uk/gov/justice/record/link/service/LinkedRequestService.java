package uk.gov.justice.record.link.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import uk.gov.justice.record.link.entity.LinkedRequest;
import uk.gov.justice.record.link.respository.LinkedRequestRepository;
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

}
