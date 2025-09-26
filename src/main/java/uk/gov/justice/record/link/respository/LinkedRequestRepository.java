package uk.gov.justice.record.link.respository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.Nullable;
import uk.gov.justice.record.link.entity.LinkedRequest;
import uk.gov.justice.record.link.entity.Status;

import java.util.Collection;
import java.util.Optional;
import java.util.List;
import java.util.UUID;

public interface LinkedRequestRepository extends JpaRepository<LinkedRequest, UUID> {

    int countByCcmsUser_LoginIdAndStatusIn(final String loginId, final Collection<Status> statuses);

    int countByOldLoginIdAndStatus(String oldLoginId, Status status);

    int countByOldLoginIdAndIdamFirmCodeAndStatusIn(String oldLoginId, String idamFirmCode, List<Status> statuses);

    Page<LinkedRequest> findByOldLoginIdContainingAllIgnoreCase(@Nullable String oldLoginId, Pageable pageable);
    
    @Query("SELECT lr FROM LinkedRequest lr WHERE "
            + "LOWER(lr.oldLoginId) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
            + "LOWER(lr.idamFirstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
            + "LOWER(lr.idamLastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
            + "LOWER(lr.idamFirmName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<LinkedRequest> searchByMultipleFields(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    Page<LinkedRequest> findByIdamLegacyUserId(String idamLegacyUserId, Pageable pageable);

    Page<LinkedRequest> findByLaaAssignee(String laaAssignee, Pageable pageable);
    
    Optional<LinkedRequest> findFirstByLaaAssigneeIsNullAndStatusOrderByCreatedDateAsc(Status status);
}