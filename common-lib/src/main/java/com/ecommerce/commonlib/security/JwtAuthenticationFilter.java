package com.ecommerce.commonlib.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenValidator jwtTokenValidator;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // No token present — pass through, Spring Security will
        // reject if the endpoint requires authentication
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String rawToken = authHeader.substring(7);
        TokenValidationResult result = jwtTokenValidator.validate(rawToken);

        if (!result.valid()) {
            // Token present but invalid — log and continue without setting auth.
            // Spring Security will return 401 for protected endpoints.
            log.warn("Invalid JWT on request to {}: {}",
                    request.getRequestURI(), result.failureReason());
            chain.doFilter(request, response);
            return;
        }

        // Token is valid — extract claims and build Spring Security context
        Claims claims = result.claims();
        String email = claims.getSubject();
        String role = claims.get("role", String.class);
        Long userId = claims.get("userId", Long.class);

        // Store UserContext as request attribute
        // — accessible in any controller via:
        // (UserContext) request.getAttribute("userContext")
        UserContext userContext = new UserContext(userId, email, role);
        request.setAttribute("userContext", userContext);

        // Build Spring Security Authentication object
        // principal   = email (who they are)
        // credentials = rawToken (the JWT itself — needed for forwarding)
        // authorities = role (what they can do)
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        email,
                        rawToken,  // ← stored here for WebClient to pick up
                        List.of(new SimpleGrantedAuthority(role))
                );

        // Set in SecurityContext — from here Spring Security
        // knows the request is authenticated
        SecurityContextHolder.getContext().setAuthentication(authentication);

        log.debug("Authenticated request: email={} role={} path={}",
                email, role, request.getRequestURI());

        chain.doFilter(request, response);
    }
}