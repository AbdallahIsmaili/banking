package com.securitybanking.auth.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2FailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2FailureHandler.class);

    @Value("${frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        logger.error("OAuth2 authentication failed", exception);

        String errorMessage = "OAuth2 authentication failed";
        if (exception.getMessage() != null) {
            errorMessage = exception.getMessage();
        }

        String failureUrl = String.format("%s/login?error=oauth2_failed&message=%s",
                frontendUrl, java.net.URLEncoder.encode(errorMessage, "UTF-8"));

        logger.debug("Redirecting to failure URL: {}", failureUrl);
        getRedirectStrategy().sendRedirect(request, response, failureUrl);
    }
}