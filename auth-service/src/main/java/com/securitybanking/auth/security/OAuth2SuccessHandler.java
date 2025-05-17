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
        logger.debug("OAuth2 authentication success");
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oAuth2User = oauthToken.getPrincipal();

        // Get user details from OAuth provider
        Map<String, Object> attributes = oAuth2User.getAttributes();
        logger.debug("OAuth2 attributes: {}", attributes);

        String email = (String) attributes.get("email");
        // Handle the case where name might be in a different attribute depending on the provider
        String name;
        if (attributes.containsKey("name")) {
            name = (String) attributes.get("name");
        } else if (attributes.containsKey("given_name") && attributes.containsKey("family_name")) {
            name = attributes.get("given_name") + " " + attributes.get("family_name");
        } else {
            name = email.substring(0, email.indexOf('@'));
        }
        final String finalName = name;
        final String finalEmail = email;


        try {
            // Look for the user in our database or create them
            AppUser user = userRepository.findByEmail(finalEmail)
                    .orElseGet(() -> {
                        AppUser newUser = new AppUser();
                        newUser.setEmail(finalEmail);
                        newUser.setFullname(finalName);
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

            // Redirect to frontend with tokens as URL parameters
            // In production, consider using a more secure method
            String frontendRedirectUrl = frontendUrl + "/oauth-success?token=" +
                    accessToken + "&refreshToken=" + refreshToken;

            logger.debug("Redirecting to: {}", frontendRedirectUrl);
            getRedirectStrategy().sendRedirect(request, response, frontendRedirectUrl);
        } catch (Exception e) {
            logger.error("Error in OAuth2 success handler", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error processing OAuth2 login");
        }
    }
}