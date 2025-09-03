package uk.gov.justice.record.link.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import uk.gov.justice.record.link.constants.ValidationConstants;
import uk.gov.justice.record.link.model.UserTransferRequest;
import uk.gov.justice.record.link.service.UserTransferService;
import uk.gov.justice.record.link.validation.groups.OnCreateRequest;
import uk.gov.justice.record.link.validation.groups.SubmissionValidationSequence;

import java.util.List;
import java.util.Objects;

/**
 * Controller for handling user transfer related requests.
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/external")
@Slf4j
public class UserTransferController {

    private final UserTransferService userTransferService;

    @GetMapping("/")
    public String homepage(@AuthenticationPrincipal OidcUser oidcUse) {
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
    public String userTransferRequest(@Validated(OnCreateRequest.class) @ModelAttribute UserTransferRequest userTransferRequest,
                                      BindingResult result, Model model, HttpSession session) {
        if (result.hasErrors()) {
            return "user-transfer-request";
        }
        model.addAttribute("userTransferRequest", userTransferRequest);
        session.setAttribute("userTransferRequest", userTransferRequest);

        return "check-answers";
    }

    @PostMapping("/request-confirmation")
    public String userLinked(@Validated(SubmissionValidationSequence.class) @ModelAttribute UserTransferRequest userTransferRequest, BindingResult result, Model model, HttpSession session) {
        log.info("User transfer request received with login id: {}", userTransferRequest.getOldLogin());
        final List<String> expectedErrorMessages = List.of(ValidationConstants.INVALID_LOGIN_ID_MESSAGE, ValidationConstants.CCMS_ACCOUNT_CLOSED,
                ValidationConstants.INVALID_STATUS_MESSAGE);
        final List<String> errors = result.getAllErrors().stream().map(ObjectError::getDefaultMessage)
                .filter(expectedErrorMessages::contains).toList();

        if (!errors.isEmpty()) {
            log.error("Invalid user transfer request with login id: {}", userTransferRequest.getOldLogin());
            userTransferService.rejectRequest(userTransferRequest, errors.getFirst());
            return "request_rejected";
        }
        if (Objects.isNull(userTransferRequest)) {
            userTransferRequest = new UserTransferRequest();
        }

        model.addAttribute("userTransferRequest", userTransferRequest);
        userTransferService.save(userTransferRequest);
        log.info("User transfer request created with login id: {}", userTransferRequest.getOldLogin());
        return "request-created";
    }

    @GetMapping("/cancel")
    public String cancelUserCreation(HttpSession session) {
        session.removeAttribute("userTransferRequest");
        return "redirect:/";
    }
}
