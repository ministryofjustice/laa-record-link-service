package uk.gov.justice.record.link.service;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/***
 * Controller for handling login-related requests.
 */
@Controller
public class LoginController {

    @GetMapping("/")
    public String login() {
        return "index";
    }

    @GetMapping("/login")
    public String link() {
        return "link-user";
    }
}
