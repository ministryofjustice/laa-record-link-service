package uk.gov.justice.record.link.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.gov.justice.record.link.constants.SilasConstants;
import uk.gov.justice.record.link.dto.CcmsUserDto;
import uk.gov.justice.record.link.entity.CcmsUser;
import uk.gov.justice.record.link.entity.LinkedRequest;
import uk.gov.justice.record.link.entity.Status;
import uk.gov.justice.record.link.model.PagedUserRequest;
import uk.gov.justice.record.link.service.LinkedRequestService;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/internal")
public class ManageLinkingAccountRequestsController {

    private final LinkedRequestService linkedRequestService;

    private String escapeCsv(String input) {
        if (input == null) {
            return "";
        }
        if (input.contains(",") || input.contains("\"") || input.contains("\n") || input.contains("\r")) {
            return "\"" + input.replace("\"", "\"\"") + "\"";
        }
        return input;
    }

    private String formatDate(LocalDateTime date, DateTimeFormatter formatter) {
        return date != null ? date.format(formatter) : "";
    }

    DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;

    private String formatStatus(Status status) {
        return status != null ? status.name() : "";
    }

    private String formatLoginId(CcmsUser ccmsUser) {
        return ccmsUser != null ? ccmsUser.getLoginId(): "";
    }

    private String fileNameDateFormatter(LocalDateTime dateTime, DateTimeFormatter formatter) {
        return dateTime.format(formatter);
    }

    @GetMapping("/download-link-account-data")
    public void downloadData(HttpServletResponse response) throws IOException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

        String timestamp = fileNameDateFormatter(LocalDateTime.now(), formatter);

        String filename = "account_transfer_" + timestamp + ".csv";

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        try (PrintWriter writer = response.getWriter()) {
            writer.println("provided_old_login_id,firm_name,vendor_site_code,creation_date,assigned_date,decision_date,status,decision_reason,laa_assignee,login_id");

            List<LinkedRequest> linkedRequests = linkedRequestService.getAllLinkedAccounts();
            for (LinkedRequest request : linkedRequests) {
                String row = String.format(Locale.ROOT, "%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
                        request.getOldLoginId(), // Provided old login
                        escapeCsv(request.getIdamFirmName()),
                        escapeCsv(request.getAdditionalInfo()), // vendor site code?
                        escapeCsv(formatDate(request.getCreatedDate(), dateFormatter)),
                        escapeCsv(formatDate(request.getAssignedDate(), dateFormatter)),
                        escapeCsv(formatDate(request.getDecisionDate(), dateFormatter)),
                        escapeCsv(formatStatus(request.getStatus())),
                        escapeCsv(request.getDecisionReason()),
                        escapeCsv(request.getLaaAssignee()),
                        escapeCsv(formatLoginId(request.getCcmsUser())));
                writer.println(row);
            }
        }
    }

    @GetMapping("/manage-linking-account")
    public String manageRequests(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(name = "oldLoginId", required = false, defaultValue = "") String oldLoginId,
            @RequestParam(defaultValue = "1") int assignedPage,
            @AuthenticationPrincipal OidcUser oidcUser,
            Model model) {

        Page<LinkedRequest> linkedRequestsPage = linkedRequestService.getLinkingRequestByOldLogin(oldLoginId, page, size);

        // Create paged request for all cases
        PagedUserRequest<LinkedRequest> pagedRequest = new PagedUserRequest<>(
                linkedRequestsPage.getContent(),
                size,
                linkedRequestsPage.getTotalPages(),
                linkedRequestsPage.getTotalElements(),
                page,
                linkedRequestsPage.hasNext(),
                linkedRequestsPage.hasPrevious()
        );

        String userName = "oidcUser.getClaims().get(SilasConstants.USER_EMAIL).toString()"; // DO NOT COMMIT

        // Get assigned requests for "Assigned cases" tab with separate pagination
        Page<LinkedRequest> assignedRequestsPage = linkedRequestService.getAssignedRequests(userName, assignedPage, size);

        // Create paged request for assigned cases
        PagedUserRequest<LinkedRequest> assignedPagedRequest = new PagedUserRequest<>(
                assignedRequestsPage.getContent(),
                size,
                assignedRequestsPage.getTotalPages(),
                assignedRequestsPage.getTotalElements(),
                assignedPage,
                assignedRequestsPage.hasNext(),
                assignedRequestsPage.hasPrevious()
        );

        model.addAttribute("pagedRequest", pagedRequest);
        model.addAttribute("assignedPagedRequest", assignedPagedRequest);
        return "manage-link-account-requests";
    }

    @GetMapping("/manage-linking-account/check-user-details")
    public String viewUserDetails(@RequestParam("id") String id, Model model,
                                  @AuthenticationPrincipal OidcUser oidcUser) {
        LinkedRequest request = linkedRequestService.getRequestById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (request.getCcmsUser() == null) {
            model.addAttribute("user", request);
            model.addAttribute("ccmsuser", null);
        } else {
            CcmsUserDto ccmsUserDto = CcmsUserDto.builder()
                    .firmName(request.getCcmsUser().getFirmName())
                    .firmCode(request.getCcmsUser().getFirmCode())
                    .firstName(request.getCcmsUser().getFirstName())
                    .lastName(request.getCcmsUser().getLastName())
                    .email(request.getCcmsUser().getEmail())
                    .loginId(request.getCcmsUser().getLoginId())
                    .build();

            model.addAttribute("user", request);
            model.addAttribute("ccmsuser", ccmsUserDto);
        }
        model.addAttribute("loggedinUserEmail", "oidcUser.getClaims().get(SilasConstants.USER_EMAIL).toString()");// DO NOT COMMIT

        return "check-user-details";
    }

    @PostMapping("/assign-next-case")
    public String assignNextCase(@AuthenticationPrincipal OidcUser oidcUser, RedirectAttributes redirectAttributes) {
        String assigneeEmail = "oidcUser.getClaims().get(SilasConstants.USER_EMAIL).toString()";// DO NOT COMMIT

        Optional<LinkedRequest> assignedRequest = linkedRequestService.assignNextCase(assigneeEmail);
        
        if (assignedRequest.isPresent()) {
            redirectAttributes.addFlashAttribute("assignmentSuccess", true);
            return "redirect:/internal/manage-linking-account/check-user-details?id=" + assignedRequest.get().id;
        } else {
            redirectAttributes.addFlashAttribute("noRequestsAvailable", true);
            return "redirect:/internal/manage-linking-account";
        }
    }

    @PostMapping("/manage-linking-account/manage/{id}/decision")
    public String processDecision(@PathVariable String id,
                                @RequestParam String decision,
                                Model model) {
        LinkedRequest request = linkedRequestService.getRequestById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        model.addAttribute("user", request);
        model.addAttribute("decision", decision);

        return "decision-reason";
    }

    @PostMapping("/manage-linking-account/manage/{id}/submit-decision")
    public String submitDecision(@PathVariable String id,
                               @RequestParam String decision,
                               @RequestParam String decisionReason,
                               RedirectAttributes redirectAttributes) {

        LinkedRequest request = linkedRequestService.getRequestById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        linkedRequestService.updateRequestDecision(id, decision, decisionReason);

        redirectAttributes.addFlashAttribute("decision", decision);
        redirectAttributes.addFlashAttribute("user", request);

        return "redirect:/internal/manage-linking-account/decision-success/" + id;
    }

    @GetMapping("/manage-linking-account/decision-success/{id}")
    public String decisionSuccess(@PathVariable String id, Model model) {
        LinkedRequest request = linkedRequestService.getRequestById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        model.addAttribute("user", request);
        model.addAttribute("decision", request.getStatus().name());

        return "request-decision-success";
    }

    @GetMapping({"", "/"})
    public String redirectToManageLinkingAccount() {
        return "redirect:/internal/manage-linking-account";
    }
    
}
