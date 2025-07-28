package uk.gov.justice.record.link.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import uk.gov.justice.record.link.entity.LinkedRequest;
import uk.gov.justice.record.link.respository.LinkedRequestRepository;

@Service
public class LinkedRequestService {

    private final LinkedRequestRepository linkedRequestRepository;

    public LinkedRequestService(LinkedRequestRepository linkedRequestRepository) {
        this.linkedRequestRepository = linkedRequestRepository;
    }

    public Page<LinkedRequest> getAllLinkingRequests(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Order.asc("createdDate")));

        return linkedRequestRepository.findAll(pageable);
    }
}
