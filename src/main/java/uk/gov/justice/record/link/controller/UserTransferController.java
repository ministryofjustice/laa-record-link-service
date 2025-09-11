package uk.gov.justice.record.link.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.justice.record.link.constants.SilasConstants;
import uk.gov.justice.record.link.constants.ValidationConstants;
import uk.gov.justice.record.link.entity.LinkedRequest;
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
    public String homepage(@RequestParam(defaultValue = "0") int page, Model model,  @AuthenticationPrincipal OidcUser oidcUser) {
        String currentUserId = oidcUser.getClaims().get(SilasConstants.SILAS_LOGIN_ID).toString();

        Pageable pageable = PageRequest.of(page, 10);
        Page<LinkedRequest> userRequests = userTransferService.getRequestsForCurrentUser(currentUserId, pageable);

        model.addAttribute("userRequests", userRequests.getContent());
        model.addAttribute("currentPage", userRequests.getNumber());
        model.addAttribute("totalPages", userRequests.getTotalPages());

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
                                      BindingResult result, Model model, HttpSession session, @AuthenticationPrincipal OidcUser oidcUser) {
        if (result.hasErrors()) {
            return "user-transfer-request";
        }
        mapToUserTransferRequest(userTransferRequest, oidcUser);
        model.addAttribute("userTransferRequest", userTransferRequest);
        session.setAttribute("userTransferRequest", userTransferRequest);

        return "check-answers";
    }

    @PostMapping("/request-confirmation")
    public String userLinked(@Validated(SubmissionValidationSequence.class) @ModelAttribute UserTransferRequest userTransferRequest, BindingResult result, Model model, HttpSession session) {
        log.info("User transfer request received with login id: {}", userTransferRequest.getOldLogin());
        final List<String> expectedErrorMessages = List.of(ValidationConstants.INVALID_LOGIN_ID_MESSAGE,
                                                           ValidationConstants.INVALID_STATUS_MESSAGE,
                                                           ValidationConstants.CCMS_ACCOUNT_CLOSED,
                                                           ValidationConstants.INVALID_FIRM_ID_MESSAGE);

        final List<String> errors = result.getAllErrors().stream().map(ObjectError::getDefaultMessage)
                .filter(expectedErrorMessages::contains).toList();

        if (!errors.isEmpty()) {
            log.error("Invalid user transfer request with login id: {}", userTransferRequest.getOldLogin());
            userTransferService.rejectRequest(userTransferRequest, errors.getFirst());
            return "request_rejected";
        }

        model.addAttribute("userTransferRequest", userTransferRequest);
        userTransferService.createRequest(userTransferRequest);
        log.info("User transfer request created with login id: {}", userTransferRequest.getOldLogin());
        return "request-created";
    }

    @GetMapping("/cancel")
    public String cancelUserCreation(HttpSession session) {
        session.removeAttribute("userTransferRequest");
        return "redirect:/external/";
    }

    private void mapToUserTransferRequest(final UserTransferRequest userTransferRequest, final OidcUser oidcUser) {
        userTransferRequest.setLegacyUserId(oidcUser.getClaims().get(SilasConstants.SILAS_LOGIN_ID).toString());
        userTransferRequest.setFirstName(oidcUser.getClaims().get(SilasConstants.FIRST_NAME).toString());
        userTransferRequest.setLastName(oidcUser.getClaims().get(SilasConstants.SURNAME).toString());
        userTransferRequest.setFirmCode(oidcUser.getClaims().get(SilasConstants.FIRM_CODE).toString());
        userTransferRequest.setFirmName(oidcUser.getClaims().get(SilasConstants.FIRM_NAME).toString());
        userTransferRequest.setEmail(oidcUser.getClaims().get(SilasConstants.USER_EMAIL).toString());
    }
}
