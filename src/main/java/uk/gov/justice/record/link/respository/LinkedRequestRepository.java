package uk.gov.justice.record.link.respository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.justice.record.link.entity.LinkedRequest;
import uk.gov.justice.record.link.entity.Status;

import java.util.Collection;
import java.util.UUID;

public interface LinkedRequestRepository extends JpaRepository<LinkedRequest, UUID> {

    int countByCcmsUser_LoginIdAndStatusIn(final String loginId, final Collection<Status> statuses);

}


