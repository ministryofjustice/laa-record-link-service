package uk.gov.justice.record.link.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import uk.gov.justice.record.link.model.UserTransferRequest;

import java.util.Objects;

/**
 * Controller for handling login-related requests.
 */
@Controller
public class LoginController {

    @GetMapping("/")
    public String login() {
        return "index";
    }

    @GetMapping("/login")
    public String transferRequest(Model model, HttpSession session) {
        UserTransferRequest user = (UserTransferRequest) session.getAttribute("user");
        if (Objects.isNull(user)) {
            user = new UserTransferRequest();
        }
        model.addAttribute("user", user);
        return "link-user";
    }

    @PostMapping("/check-answers")
    public String addUserCheckAnswers(@ModelAttribute UserTransferRequest user, Model model, HttpSession session) {
        model.addAttribute("user", user);
        session.setAttribute("user", user);
        return "check-answers";
    }
}
