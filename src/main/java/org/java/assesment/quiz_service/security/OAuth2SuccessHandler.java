package org.java.assesment.quiz_service.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.java.assesment.quiz_service.model.AppUser;
import org.java.assesment.quiz_service.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AppUserRepository userRepository;
    private final JwtService jwtService;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email     = oAuth2User.getAttribute("email");
        String googleSub = oAuth2User.getAttribute("sub");
        String name      = oAuth2User.getAttribute("name");

        AppUser user = userRepository.findByGoogleSub(googleSub)
                .orElseGet(() -> userRepository.findByEmail(email)
                        .map(existing -> {
                            existing.setGoogleSub(googleSub);
                            return userRepository.save(existing);
                        })
                        .orElseGet(() -> userRepository.save(
                                AppUser.builder()
                                        .email(email)
                                        .googleSub(googleSub)
                                        .displayName(name)
                                        .build()
                        ))
                );

        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole().name());

        // Redirect to frontend with token in query param — frontend stores it in localStorage
        getRedirectStrategy().sendRedirect(request, response,
                frontendUrl + "/oauth2/callback?token=" + token);
    }
}
