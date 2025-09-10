package uk.gov.justice.record.link.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.justice.record.link.entity.LinkedRequest;
import uk.gov.justice.record.link.model.PagedUserRequest;
import uk.gov.justice.record.link.service.CurrentUserService;
import uk.gov.justice.record.link.service.LinkedRequestService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/internal")
public class ManageLinkingAccountRequestsController {

    private final LinkedRequestService linkedRequestService;
    private final CurrentUserService currentUserService;

    @GetMapping("/manage-linking-account")
    public String manageRequests(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(name = "oldLoginId", required = false, defaultValue = "") String oldLoginId,
            @RequestParam(defaultValue = "1") int assignedPage,
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

        String userName = currentUserService.getUserName();

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
}
