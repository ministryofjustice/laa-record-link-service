package uk.gov.justice.record.link.service.respository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.justice.record.link.service.entity.LinkedRequest;

import java.util.UUID;

public interface LinkedRequestRepository extends JpaRepository<LinkedRequest, UUID> {
}
