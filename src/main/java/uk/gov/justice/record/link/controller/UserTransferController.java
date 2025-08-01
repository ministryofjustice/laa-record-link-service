package uk.gov.justice.record.link.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import uk.gov.justice.record.link.model.UserTransferRequest;
import uk.gov.justice.record.link.service.UserTransferService;

import java.util.Objects;

/**
 * Controller for handling user transfer related requests.
 */
@Controller
@RequiredArgsConstructor
public class UserTransferController {

    private final UserTransferService userTransferService;

    @GetMapping("/")
    public String homepage() {
        return "index";
    }

    @GetMapping("/user-transfer-request")
    public String userTransferRequest(Model model, HttpSession session) {
        UserTransferRequest userTransferRequest = (UserTransferRequest) session.getAttribute("userTransferRequest");

        if (Objects.isNull(userTransferRequest)) {
            userTransferRequest = new UserTransferRequest();
        }
        model.addAttribute("userTransferRequest", userTransferRequest);
        return "user-transfer-request";
    }

    @PostMapping("/check-answers")
    public String userTransferRequest(@Valid @ModelAttribute UserTransferRequest userTransferRequest,
                                      BindingResult result, Model model, HttpSession session) {

        if (result.hasErrors() && result.getAllErrors().stream().anyMatch(er -> er.getDefaultMessage().equals("Enter CCMS username"))) {
            return "user-transfer-request";
        }
        model.addAttribute("userTransferRequest", userTransferRequest);
        session.setAttribute("userTransferRequest", userTransferRequest);

        return "check-answers";
    }

    @PostMapping("/request-confirmation")
    public String userLinked(@Valid @ModelAttribute UserTransferRequest userTransferRequest, BindingResult result, Model model, HttpSession session) {
        if (result.hasErrors() && result.getAllErrors().stream().anyMatch(er -> er.getDefaultMessage().equals("Invalid Request"))) {
            return "request_rejected";
        }
        if (Objects.isNull(userTransferRequest)) {
            userTransferRequest = new UserTransferRequest();
        }

        model.addAttribute("userTransferRequest", userTransferRequest);
        userTransferService.save(userTransferRequest);
        return "request-created";
    }

    @GetMapping("/cancel")
    public String cancelUserCreation(HttpSession session) {
        session.removeAttribute("userTransferRequest");
        return "redirect:/";
    }
}
