package uk.gov.justice.record.link.respository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.justice.record.link.entity.LinkedRequest;

import java.util.UUID;

public interface LinkedRequestRepository extends JpaRepository<LinkedRequest, UUID> {
}
