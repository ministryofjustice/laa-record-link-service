package uk.gov.justice.record.link.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthorisationController {

    @GetMapping("/not-authorised")
    public String errorHome() {
        return "not-authorised";
    }
}