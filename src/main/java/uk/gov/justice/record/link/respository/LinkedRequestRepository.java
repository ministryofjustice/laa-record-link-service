package uk.gov.justice.record.link.respository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.Nullable;
import uk.gov.justice.record.link.entity.LinkedRequest;
import uk.gov.justice.record.link.entity.Status;

import java.util.Collection;
import java.util.UUID;

public interface LinkedRequestRepository extends JpaRepository<LinkedRequest, UUID> {

    int countByCcmsUser_LoginIdAndStatusIn(final String loginId, final Collection<Status> statuses);

    Page<LinkedRequest> findByOldLoginIdContainingAllIgnoreCase(@Nullable String oldLoginId, Pageable pageable);
    
    Page<LinkedRequest> findByIdamLegacyUserId(String idamLegacyUserId, Pageable pageable);
    
    Page<LinkedRequest> findByLaaAssignee(String laaAssignee, Pageable pageable);
}


