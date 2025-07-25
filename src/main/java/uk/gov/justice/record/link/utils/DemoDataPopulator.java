package uk.gov.justice.record.link.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.justice.record.link.entity.CcmsUser;
import uk.gov.justice.record.link.entity.LinkedRequest;
import uk.gov.justice.record.link.entity.Status;
import uk.gov.justice.record.link.respository.CcmsUserRepository;
import uk.gov.justice.record.link.respository.LinkedRequestRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@Profile("!production")
public class DemoDataPopulator {

    @Value("${app.populate.dummy-data}")
    private boolean populateDummyData;

    @Value("${app.test.internal.userPrincipals}")
    private Set<String> internalUserPrincipals;

    @Autowired
    private CcmsUserRepository ccmsUserRepository;

    @Autowired
    private LinkedRequestRepository linkedRequestRepository;

    @EventListener
    public void appReady(ApplicationReadyEvent event) {
        if (populateDummyData) {
            populateDummyData();
        }
    }

    private void populateDummyData() {
        try {
            CcmsUser result = ccmsUserRepository.findByLoginId("jsmith001");
            if (result == null) {
                createUserWithMultipleRequests();
                System.out.println("Dummy Data Populated!!");
            }
            if (!internalUserPrincipals.isEmpty()) {
                internalUserPrincipals.forEach(
                        email -> {
                            if (ccmsUserRepository.findByEmail(email) == null) {
                                CcmsUser ccmsUser = createCcmsUser("123", "Internal", "Principal", "Internal Firm", "F123", email);
                                ccmsUser = ccmsUserRepository.save(ccmsUser);
                                createLinkedRequest(ccmsUser, "Idam", "Name", "idam1@gmail.com", "Idam Firm 1", "IF1", Status.APPROVED, null, null);
                                createLinkedRequest(ccmsUser, "Idam", "Name", "idam2@gmail.com", "Idam Firm 2", "IF2", Status.REJECTED, null, null);
                                createLinkedRequest(ccmsUser, "Idam", "Name", "idam3@gmail.com", "Idam Firm 3", "IF3", Status.OPEN, null, null);
                                System.out.println("Dummy internal users Populated!!");
                            }
                        });
            }
        } catch (Exception ex) {
            System.err.println("Error populating dummy data!!");
            System.err.println(ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void createUserWithMultipleRequests() {
        CcmsUser user2 = createCcmsUser("mjones002", "Mary", "Jones", "Jones Legal Services", "JLS002", "mary.jones@joneslaw.co.uk");
        user2 = ccmsUserRepository.save(user2);

        createLinkedRequest(user2, "Carol", "Davis", "carol.davis@example.com", "Davis Law Firm", "DLF001", Status.OPEN, null, null);
        createLinkedRequest(user2, "David", "Miller", "david.miller@example.com", "Miller & Associates", "MA001", Status.APPROVED, "laa.admin1", "Verified identity and firm details");
        createLinkedRequest(user2, "Emma", "Taylor", "emma.taylor@example.com", "Taylor Legal Services", "TLS001", Status.REJECTED, "laa.admin3", "Unable to verify firm association");
    }

    private CcmsUser createCcmsUser(String loginId, String firstName, String lastName, String firmName, String firmCode, String email) {
        return CcmsUser.builder()
                .loginId(loginId)
                .firstName(firstName)
                .lastName(lastName)
                .firmName(firmName)
                .firmCode(firmCode)
                .email(email)
                .build();
    }

    private void createLinkedRequest(CcmsUser ccmsUser, String idamFirstName, String idamLastName,
                                           String idamEmail, String idamFirmName, String idamFirmCode, 
                                           Status status, String laaAssignee, String additionalInfo) {
        LocalDateTime now = LocalDateTime.now();
        LinkedRequest request = LinkedRequest.builder()
                .ccmsUser(ccmsUser)
                .idamLegacyUserId(UUID.randomUUID())
                .idamFirstName(idamFirstName)
                .idamLastName(idamLastName)
                .idamEmail(idamEmail)
                .idamFirmName(idamFirmName)
                .idamFirmCode(idamFirmCode)
                .status(status)
                .createdDate(now.minusDays(15))
                .laaAssignee(laaAssignee)
                .additionalInfo(additionalInfo)
                .build();

        if (status == Status.APPROVED || status == Status.REJECTED) {
            request = request.toBuilder()
                    .decisionDate(now.minusDays(5))
                    .decisionReason(status == Status.APPROVED ? "Identity verified successfully" : "Unable to verify identity")
                    .assignedDate(now.minusDays(10))
                    .build();
        }

        linkedRequestRepository.save(request);
    }

}
