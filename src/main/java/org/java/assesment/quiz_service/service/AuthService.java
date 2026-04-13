package org.java.assesment.quiz_service.service;

import lombok.RequiredArgsConstructor;
import org.java.assesment.quiz_service.dto.auth.AuthResponse;
import org.java.assesment.quiz_service.dto.auth.LoginRequest;
import org.java.assesment.quiz_service.dto.auth.RegisterRequest;
import org.java.assesment.quiz_service.model.AppUser;
import org.java.assesment.quiz_service.repository.AppUserRepository;
import org.java.assesment.quiz_service.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already registered: " + request.email());
        }

        AppUser user = userRepository.save(
                AppUser.builder()
                        .email(request.email())
                        .passwordHash(passwordEncoder.encode(request.password()))
                        .displayName(request.displayName())
                        .build()
        );

        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        return new AuthResponse(token, user.getEmail(), user.getDisplayName(), user.getRole().name());
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        AppUser user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (user.getPasswordHash() == null) {
            throw new IllegalArgumentException("This account uses Google sign-in. Please use 'Sign in with Google'.");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        return new AuthResponse(token, user.getEmail(), user.getDisplayName(), user.getRole().name());
    }
}
