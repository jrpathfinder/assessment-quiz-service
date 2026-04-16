package org.java.assesment.quiz_service.config;

import org.java.assesment.quiz_service.security.JwtAuthenticationFilter;
import org.java.assesment.quiz_service.security.OAuth2SuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    /** Null when GOOGLE_CLIENT_ID is not configured (local dev / no SSO) */
    @Autowired(required = false)
    private OAuth2SuccessHandler oAuth2SuccessHandler;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            // IF_REQUIRED: sessions only for OAuth2 code flow (stores PKCE code_verifier)
            // JWT-authenticated API calls don't create sessions
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                // Read-only quiz data is public (browse without login)
                .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/exams/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/questions/**").permitAll()
                // Exam-taking endpoints: service handles anonymous users (null user is OK)
                // Admin-only actions (create/update/delete) are protected by @PreAuthorize
                .requestMatchers(HttpMethod.POST, "/api/exams/*/start").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/attempts/*/submit").permitAll()
                .requestMatchers("/api/attempts/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            // Return 401 (not 403) for unauthenticated requests — lets the frontend redirect to /login
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            );

        // Only wire OAuth2 login when Google credentials are present
        if (oAuth2SuccessHandler != null) {
            http.oauth2Login(oauth -> oauth.successHandler(oAuth2SuccessHandler));
        }

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
