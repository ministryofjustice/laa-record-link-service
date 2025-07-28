package uk.gov.justice.record.link.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.justice.record.link.entity.LinkedRequest;
import uk.gov.justice.record.link.service.LinkedRequestService;

@Controller
@RequiredArgsConstructor
public class ManageLinkingAccountRequestsController {

    private final LinkedRequestService linkedRequestService;

    @GetMapping("/manage-linking-account")
    public String manageRequests(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        Page<LinkedRequest> linkedRequestsPage = linkedRequestService.getAllLinkingRequests(page, size);

        model.addAttribute("linkedRequests", linkedRequestsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", linkedRequestsPage.getTotalPages());
        model.addAttribute("totalItems", linkedRequestsPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("hasPrevious", linkedRequestsPage.hasPrevious());
        model.addAttribute("hasNext", linkedRequestsPage.hasNext());
        
        return "manage-link-account-requests";
    }
}
