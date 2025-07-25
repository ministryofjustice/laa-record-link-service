package uk.gov.justice.record.link.respository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.justice.record.link.entity.CcmsUser;

import java.util.List;
import java.util.UUID;

public interface CcmsUserRepository extends JpaRepository<CcmsUser, UUID> {
    CcmsUser findByLoginId(String loginId);

    CcmsUser findByEmail(String email);
}
