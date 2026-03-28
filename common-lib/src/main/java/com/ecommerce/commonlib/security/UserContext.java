package com.ecommerce.commonlib.security;

public record UserContext(
        Long userId,
        String email,
        String role
) {
    public boolean isAdmin() {
        return "ROLE_ADMIN".equals(role);
    }

    public boolean isUser() {
        return "ROLE_USER".equals(role);
    }

    // Convenience — strips ROLE_ prefix for display
    public String getRoleSimple() {
        return role != null ? role.replace("ROLE_", "") : null;
    }
}