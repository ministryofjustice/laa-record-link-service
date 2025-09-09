package uk.gov.justice.record.link.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import uk.gov.justice.record.link.entity.LinkedRequest;
import uk.gov.justice.record.link.model.PagedUserRequest;
import uk.gov.justice.record.link.service.LinkedRequestService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/internal")
public class ManageLinkingAccountRequestsController {

    private final LinkedRequestService linkedRequestService;

    @GetMapping("/manage-linking-account")
    public String manageRequests(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(name = "oldLoginId", required = false, defaultValue = "") String oldLoginId,
            Model model) {

        Page<LinkedRequest> linkedRequestsPage = linkedRequestService.getLinkingRequestByOldLogin(oldLoginId, page, size);

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
        return "manage-link-account-requests";
    }

    @GetMapping("/manage-linking-account/check-user-details")
    public String viewUserDetails(@RequestParam("id") String id, Model model) {
        System.out.println("Received id: " + id);

        LinkedRequest request = linkedRequestService.getRequestById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        model.addAttribute("user", request);
        return "check-user-details"; 
    }


}
