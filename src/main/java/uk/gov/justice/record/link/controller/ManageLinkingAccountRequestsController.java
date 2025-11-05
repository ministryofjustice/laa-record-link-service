package uk.gov.justice.record.link.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.gov.justice.record.link.constants.SilasConstants;
import uk.gov.justice.record.link.dto.CcmsUserDto;
import uk.gov.justice.record.link.entity.LinkedRequest;
import uk.gov.justice.record.link.model.PagedUserRequest;
import uk.gov.justice.record.link.service.DataDownloadService;
import uk.gov.justice.record.link.service.LinkedRequestService;
import uk.gov.justice.record.link.service.OidcTokenClaimsExtractor;
import uk.gov.justice.record.link.util.UserRoleIdentifier;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/internal")
public class ManageLinkingAccountRequestsController {

    private final LinkedRequestService linkedRequestService;

    private final DataDownloadService dataDownloadService;

    @GetMapping("/download-link-account-data")
    public void downloadData(HttpServletResponse response) throws IOException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

        String timestamp = dataDownloadService.fileNameDateFormatter(LocalDateTime.now(), formatter);

        String filename = "account_transfer_" + timestamp + ".csv";

        String columns = "provided_old_login_id,firm_name,vendor_site_code,"
                + "creation_date,assigned_date,decision_date,status,decision_reason,laa_assignee,login_id";

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        try (PrintWriter writer = response.getWriter()) {
            List<LinkedRequest> linkedRequests = linkedRequestService.getAllLinkedAccounts();
            dataDownloadService.writeLinkedRequestsToWriter(writer, columns, linkedRequests);
        }
    }

    @GetMapping("/viewer")
    public String displayRequests(@RequestParam(defaultValue = "1") int page,
                                  @RequestParam(defaultValue = "10") int size,
                                  @RequestParam(name = "searchTerm", required = false, defaultValue = "") String searchTerm,
                                  @AuthenticationPrincipal OidcUser oidcUser,
                                  Model model
    ) {

        Page<LinkedRequest> linkedRequestsPage = linkedRequestService.searchLinkingRequests(searchTerm, page, size);

        PagedUserRequest<LinkedRequest> pagedRequest = new PagedUserRequest<>(
                linkedRequestsPage.getContent(),
                size,
                linkedRequestsPage.getTotalPages(),
                linkedRequestsPage.getTotalElements(),
                page,
                linkedRequestsPage.hasNext(),
                linkedRequestsPage.hasPrevious()
        );

        model.addAttribute("pagedRequest", pagedRequest);
        model.addAttribute("homeUrl", resolveHomeUrl(oidcUser));
        return "viewer";
    }

    @GetMapping("/manage-linking-account")
    public String manageRequests(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(name = "searchTerm", required = false, defaultValue = "") String searchTerm,
            @RequestParam(defaultValue = "1") int assignedPage,
            @AuthenticationPrincipal OidcUser oidcUser,
            Model model) {

        Page<LinkedRequest> linkedRequestsPage = linkedRequestService.searchLinkingRequests(searchTerm, page, size);

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

        String userName = oidcUser.getClaims().get(SilasConstants.USER_EMAIL).toString();

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
        model.addAttribute("homeUrl", resolveHomeUrl(oidcUser));
        return "manage-link-account-requests";
    }

    @GetMapping({"/viewer/check-user-details", "/manage-linking-account/check-user-details"})
    public String viewUserDetails(@RequestParam("id") String id, Model model,
                                  @AuthenticationPrincipal OidcUser oidcUser,
                                  HttpServletRequest request) {
        LinkedRequest linkedRequest = linkedRequestService.getRequestById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (linkedRequest.getCcmsUser() == null) {
            model.addAttribute("user", linkedRequest);
            model.addAttribute("ccmsuser", null);
        } else {
            CcmsUserDto ccmsUserDto = CcmsUserDto.builder()
                    .firmName(linkedRequest.getCcmsUser().getFirmName())
                    .firmCode(linkedRequest.getCcmsUser().getFirmCode())
                    .firstName(linkedRequest.getCcmsUser().getFirstName())
                    .lastName(linkedRequest.getCcmsUser().getLastName())
                    .email(linkedRequest.getCcmsUser().getEmail())
                    .loginId(linkedRequest.getCcmsUser().getLoginId())
                    .build();

            model.addAttribute("user", linkedRequest);
            model.addAttribute("ccmsuser", ccmsUserDto);
        }

        String loggedInUserEmail = oidcUser.getClaims().get(SilasConstants.USER_EMAIL).toString();
        model.addAttribute("loggedinUserEmail", loggedInUserEmail);
        model.addAttribute("homeUrl", resolveHomeUrl(oidcUser));
        String matchedPattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        if ("/internal/viewer/check-user-details".equals(matchedPattern)) {
            log.info("Internal user {} has viewed details of request {}", loggedInUserEmail, linkedRequest.getId());
        }
        return "check-user-details";
    }

    @PostMapping("/assign-next-case")
    public String assignNextCase(@AuthenticationPrincipal OidcUser oidcUser, RedirectAttributes redirectAttributes) {
        String assigneeEmail = oidcUser.getClaims().get(SilasConstants.USER_EMAIL).toString();

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
                                @AuthenticationPrincipal OidcUser oidcUser,
                                Model model) {
        LinkedRequest request = linkedRequestService.getRequestById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        model.addAttribute("user", request);
        model.addAttribute("decision", decision);
        model.addAttribute("homeUrl", resolveHomeUrl(oidcUser));
        OidcTokenClaimsExtractor extractor = new OidcTokenClaimsExtractor(oidcUser);
        model.addAttribute("isInternalAdmin", UserRoleIdentifier.isInternalAdmin(extractor.getRoles()));

        return "decision-reason";
    }

    @PostMapping("/manage-linking-account/manage/{id}/submit-decision")
    public String submitDecision(@PathVariable String id,
                               @RequestParam String decision,
                               @RequestParam String decisionReason,
                               RedirectAttributes redirectAttributes,
                               @AuthenticationPrincipal OidcUser oidcUser,
                               Model model) {

        LinkedRequest request = linkedRequestService.getRequestById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (decisionReason == null || decisionReason.trim().isEmpty()) {
            model.addAttribute("user", request);
            model.addAttribute("decision", decision);
            model.addAttribute("error", "Please provide a reason");
            model.addAttribute("homeUrl", resolveHomeUrl(oidcUser));
            OidcTokenClaimsExtractor extractor = new OidcTokenClaimsExtractor(oidcUser);
            model.addAttribute("isInternalAdmin", UserRoleIdentifier.isInternalAdmin(extractor.getRoles()));
            return "decision-reason";
        }

        linkedRequestService.updateRequestDecision(id, decision, decisionReason.trim());

        redirectAttributes.addFlashAttribute("decision", decision);
        redirectAttributes.addFlashAttribute("user", request);

        return "redirect:/internal/manage-linking-account/decision-success/" + id;
    }

    @GetMapping("/manage-linking-account/decision-success/{id}")
    public String decisionSuccess(@PathVariable String id,
                                  @AuthenticationPrincipal OidcUser oidcUser,
                                  Model model) {
        LinkedRequest request = linkedRequestService.getRequestById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        model.addAttribute("user", request);
        model.addAttribute("decision", request.getStatus().name());
        model.addAttribute("homeUrl", resolveHomeUrl(oidcUser));
        OidcTokenClaimsExtractor extractor = new OidcTokenClaimsExtractor(oidcUser);
        model.addAttribute("isInternalAdmin", UserRoleIdentifier.isInternalAdmin(extractor.getRoles()));

        return "request-decision-success";
    }

    @GetMapping({"", "/"})
    public String redirectToManageLinkingAccount() {
        return "redirect:/internal/manage-linking-account";
    }
    
    private String resolveHomeUrl(OidcUser oidcUser) {
        if (oidcUser == null) {
            return "/unauthorized";
        }
        OidcTokenClaimsExtractor extractor = new OidcTokenClaimsExtractor(oidcUser);
        List<String> roles = extractor.getRoles();
        if (UserRoleIdentifier.isInternalAdmin(roles)) {
            return "/internal/manage-linking-account";
        }
        if (UserRoleIdentifier.isInternalViewer(roles)) {
            return "/internal/viewer";
        }
        return "/unauthorized";
    }
}
