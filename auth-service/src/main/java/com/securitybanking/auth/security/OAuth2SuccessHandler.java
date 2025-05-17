package com.securitybanking.auth.security;

import com.securitybanking.auth.entity.AppUser;
import com.securitybanking.auth.entity.UserRole;
import com.securitybanking.auth.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public OAuth2SuccessHandler(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oAuth2User = oauthToken.getPrincipal();

        // Get user details from OAuth provider
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");

        // Look for the user in our database or create them
        AppUser user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    AppUser newUser = new AppUser();
                    newUser.setEmail(email);
                    newUser.setFullname(name);
                    // Use a random secure password since they'll use OAuth to log in
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
        String frontendRedirectUrl = "http://localhost:3000/oauth-success?token=" +
                accessToken + "&refreshToken=" + refreshToken;
        getRedirectStrategy().sendRedirect(request, response, frontendRedirectUrl);
    }
}