package com.ecommerce.commonlib.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Slf4j
public class JwtTokenValidator {

    private final SecretKey signingKey;

    // Constructor takes the hex-encoded secret string
    // Each service passes its own configured secret
    public JwtTokenValidator(String hexSecret) {
        this.signingKey = Keys.hmacShaKeyFor(hexToBytes(hexSecret));
    }

    /**
     * Core validation method.
     * Returns a TokenValidationResult — never throws.
     * Caller decides what to do with invalid tokens.
     */
    public TokenValidationResult validate(String token) {
        try {
            Claims claims = parseToken(token);
            return TokenValidationResult.valid(claims);
        } catch (ExpiredJwtException e) {
            log.warn("JWT expired: {}", e.getMessage());
            return TokenValidationResult.invalid("TOKEN_EXPIRED");
        } catch (MalformedJwtException e) {
            log.warn("JWT malformed: {}", e.getMessage());
            return TokenValidationResult.invalid("TOKEN_MALFORMED");
        } catch (SecurityException e) {
            // This is the critical one — wrong signature means tampering
            log.warn("JWT signature invalid: {}", e.getMessage());
            return TokenValidationResult.invalid("TOKEN_INVALID_SIGNATURE");
        } catch (Exception e) {
            log.warn("JWT validation failed: {}", e.getMessage());
            return TokenValidationResult.invalid("TOKEN_INVALID");
        }
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Long extractUserId(String token) {
        return extractClaim(token,
                claims -> claims.get("userId", Long.class));
    }

    public String extractRole(String token) {
        return extractClaim(token,
                claims -> claims.get("role", String.class));
    }

    public boolean isExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(parseToken(token));
    }

    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Converts hex string secret to byte array for HMAC key
    private byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}