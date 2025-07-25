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
            List<CcmsUser> result = ccmsUserRepository.findCcmsUserByLoginId("jsmith001");
            if (result.isEmpty()) {
                creatUserWithApprovedRequest();
                createUserWithMultipleRequests();
                createUserWithAssignedRequests();
                System.out.println("Dummy Data Populated!!");
            }
            if (!internalUserPrincipals.isEmpty()) {
                    internalUserPrincipals.forEach(
                        username -> {
                            if (username.contains(":") && username.split(":").length == 6) {
                                String[] split = username.split(":");
                                if (ccmsUserRepository.findCcmsUserByLoginId(split[0]).isEmpty()) {
                                    CcmsUser ccmsUser = createCcmsUser(split[0], split[1], split[2], split[3], split[4], split[5]);
                                    ccmsUser = ccmsUserRepository.save(ccmsUser);
                                    createLinkedRequest(ccmsUser, "first name", "last name", "idam1@gmail.com", split[3], split[4], Status.APPROVED, null, null);
                                    createLinkedRequest(ccmsUser, "first name", "last name", "idam2@gmail.com", split[3], split[4], Status.REJECTED, null, null);
                                    createLinkedRequest(ccmsUser, "first name", "last name", "idam3@gmail.com", split[3], split[4], Status.OPEN, null, null);
                                    System.out.println("Dummy internal users Populated!!");
                                }
                            }
                        }
                    );
            }
        } catch (Exception ex) {
            System.err.println("Error populating dummy data!!");
            System.err.println(ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void createUserWithAssignedRequests() {
        CcmsUser user3 = createCcmsUser("rbrown003", "Robert", "Brown", "Brown & Partners", "BP003", "robert.brown@brownpartners.co.uk");
        user3 = ccmsUserRepository.save(user3);

        createLinkedRequest(user3, "Frank", "Anderson", "frank.anderson@example.com", "Anderson Law", "AL001", Status.OPEN, null, null);
        LinkedRequest assignedRequest = createLinkedRequest(user3, "Grace", "Thomas", "grace.thomas@example.com", "Thomas & Partners", "TP001", Status.OPEN, "laa.admin2", "Under review");
        assignedRequest = assignedRequest.toBuilder().assignedDate(LocalDateTime.now().minusDays(2)).build();
        linkedRequestRepository.save(assignedRequest);
    }

    private void createUserWithMultipleRequests() {
        CcmsUser user2 = createCcmsUser("mjones002", "Mary", "Jones", "Jones Legal Services", "JLS002", "mary.jones@joneslaw.co.uk");
        user2 = ccmsUserRepository.save(user2);

        createLinkedRequest(user2, "Carol", "Davis", "carol.davis@example.com", "Davis Law Firm", "DLF001", Status.OPEN, null, null);
        createLinkedRequest(user2, "David", "Miller", "david.miller@example.com", "Miller & Associates", "MA001", Status.APPROVED, "laa.admin1", "Verified identity and firm details");
        createLinkedRequest(user2, "Emma", "Taylor", "emma.taylor@example.com", "Taylor Legal Services", "TLS001", Status.REJECTED, "laa.admin3", "Unable to verify firm association");
    }

    private void creatUserWithApprovedRequest() {
        CcmsUser user1 = createCcmsUser("jsmith001", "John", "Smith", "Smith & Associates", "SA001", "john.smith@smithlaw.co.uk");
        user1 = ccmsUserRepository.save(user1);

        createLinkedRequest(user1, "Alice", "Johnson", "alice.johnson@example.com", "Johnson & Co", "JC001", Status.APPROVED, "laa.admin1", "Verified identity and firm details");
        createLinkedRequest(user1, "Bob", "Wilson", "bob.wilson@example.com", "Wilson Legal", "WL001", Status.REJECTED, "laa.admin2", "Unable to verify firm association");
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

    private LinkedRequest createLinkedRequest(CcmsUser ccmsUser, String idamFirstName, String idamLastName, 
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

        return linkedRequestRepository.save(request);
    }

}
