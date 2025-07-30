package uk.gov.justice.record.link.respository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.justice.record.link.entity.CcmsUser;


public interface CcmsUserRepository extends JpaRepository<CcmsUser, String> {
}