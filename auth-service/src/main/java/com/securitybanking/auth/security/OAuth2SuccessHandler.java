package com.securitybanking.auth.security;

import com.securitybanking.auth.entity.AppUser;
import com.securitybanking.auth.entity.UserRole;
import com.securitybanking.auth.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2SuccessHandler.class);

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Value("${frontend.url:http://localhost:3000}")
    private String frontendUrl;

    public OAuth2SuccessHandler(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        logger.debug("OAuth2 authentication success handler called");

        try {
            if (!(authentication instanceof OAuth2AuthenticationToken)) {
                logger.error("Authentication is not OAuth2AuthenticationToken: {}", authentication.getClass());
                redirectToErrorPage(request, response, "Invalid authentication type");
                return;
            }

            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
            OAuth2User oAuth2User = oauthToken.getPrincipal();

            // Get user details from OAuth provider
            Map<String, Object> attributes = oAuth2User.getAttributes();
            logger.debug("OAuth2 attributes received: {}", attributes.keySet());

            String email = extractEmail(attributes);
            String name = extractName(attributes, email);

            if (email == null || email.trim().isEmpty()) {
                logger.error("No email found in OAuth2 attributes: {}", attributes);
                redirectToErrorPage(request, response, "Email not provided by OAuth2 provider");
                return;
            }

            logger.debug("Processing OAuth2 login for email: {}, name: {}", email, name);

            // Look for the user in our database or create them
            AppUser user = userRepository.findByEmail(email)
                    .orElseGet(() -> {
                        logger.debug("Creating new user from OAuth2 data");
                        AppUser newUser = new AppUser();
                        newUser.setEmail(email);
                        newUser.setFullname(name);
                        newUser.setPassword("{oauth2}");
                        newUser.setRole(UserRole.USER);
                        newUser.setEnabled(true);
                        return userRepository.save(newUser);
                    });

            // Update last login
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            // Generate tokens
            String accessToken = jwtUtil.generateAccessToken(user.getEmail(), user.getRole());
            String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

            // Store refresh token
            user.setRefreshToken(refreshToken);
            userRepository.save(user);

            // Redirect to frontend with tokens
            String frontendRedirectUrl = String.format("%s/oauth-success?token=%s&refreshToken=%s",
                    frontendUrl, accessToken, refreshToken);

            logger.debug("Redirecting to frontend: {}", frontendUrl + "/oauth-success");
            getRedirectStrategy().sendRedirect(request, response, frontendRedirectUrl);

        } catch (Exception e) {
            logger.error("Error in OAuth2 success handler", e);
            redirectToErrorPage(request, response, "Error processing OAuth2 login: " + e.getMessage());
        }
    }

    private String extractEmail(Map<String, Object> attributes) {
        // Try different possible email attribute names
        String email = (String) attributes.get("email");
        if (email == null || email.trim().isEmpty()) {
            email = (String) attributes.get("mail");
        }
        if (email == null || email.trim().isEmpty()) {
            email = (String) attributes.get("emailAddress");
        }
        return email;
    }

    private String extractName(Map<String, Object> attributes, String fallbackEmail) {
        String name = (String) attributes.get("name");

        if (name == null || name.trim().isEmpty()) {
            // Try to construct name from given_name and family_name
            String givenName = (String) attributes.get("given_name");
            String familyName = (String) attributes.get("family_name");

            if (givenName != null && familyName != null) {
                name = givenName + " " + familyName;
            } else if (givenName != null) {
                name = givenName;
            }
        }

        // Final fallback to email username
        if (name == null || name.trim().isEmpty()) {
            name = fallbackEmail.substring(0, fallbackEmail.indexOf('@'));
        }

        return name;
    }

    private void redirectToErrorPage(HttpServletRequest request, HttpServletResponse response, String errorMessage)
            throws IOException {
        String errorUrl = String.format("%s/login?error=oauth2_failed&message=%s",
                frontendUrl, java.net.URLEncoder.encode(errorMessage, "UTF-8"));
        logger.debug("Redirecting to error page: {}", errorUrl);
        getRedirectStrategy().sendRedirect(request, response, errorUrl);
    }
}