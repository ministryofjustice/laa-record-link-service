package uk.gov.justice.record.link.respository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.justice.record.link.entity.CcmsUser;

import java.util.Optional;


public interface CcmsUserRepository extends JpaRepository<CcmsUser, String> {

    Optional<CcmsUser> findByLoginId(String name);

    boolean existsByFirmCode(String firmCode);

}