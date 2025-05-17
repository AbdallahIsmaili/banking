package com.securitybanking.auth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/oauth2")
public class OAuth2Controller {

    @GetMapping("/login/google")
    public String googleLogin() {
        // This endpoint exists to have a nice URL for the login button
        // The actual OAuth2 flow is handled by Spring Security
        return "redirect:/oauth2/authorize/google";
    }
}