package org.java.assesment.quiz_service.dto.auth;

public record AuthResponse(
    String accessToken,
    String email,
    String displayName,
    String role
) {}
