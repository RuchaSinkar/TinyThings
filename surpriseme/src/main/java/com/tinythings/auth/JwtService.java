package com.tinythings.auth;

import com.tinythings.user.User;
import com.tinythings.user.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long accessTokenExpiryMs;
    private final long refreshTokenExpiryMs;
    private final UserRepository userRepository;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiry-ms:900000}") long accessTokenExpiryMs,     // 15 min
            @Value("${jwt.refresh-token-expiry-ms:2592000000}") long refreshTokenExpiryMs, // 30 days
            UserRepository userRepository
    ) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTokenExpiryMs = accessTokenExpiryMs;
        this.refreshTokenExpiryMs = refreshTokenExpiryMs;
        this.userRepository = userRepository;
    }

    public String generateAccessToken(UUID userId) {
        return buildToken(userId, accessTokenExpiryMs, "access");
    }

    public String generateRefreshToken(UUID userId) {
        return buildToken(userId, refreshTokenExpiryMs, "refresh");
    }

    private String buildToken(UUID userId, long expiryMs, String tokenType) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiryMs);

        return Jwts.builder()
                .subject(userId.toString())
                .claim("type", tokenType)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();
    }

    /**
     * Validates an access token and builds a Spring Security Authentication from it.
     * Returns empty if the token is invalid, expired, or not an access token.
     */
    public Optional<Authentication> authenticateFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            if (!"access".equals(claims.get("type", String.class))) {
                return Optional.empty();
            }

            UUID userId = UUID.fromString(claims.getSubject());

            // Confirms the user still exists (handles deleted accounts with old tokens floating around)
            Optional<User> user = userRepository.findById(userId);
            if (user.isEmpty()) {
                return Optional.empty();
            }

            Authentication auth = new UsernamePasswordAuthenticationToken(
                    userId, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
            );
            return Optional.of(auth);

        } catch (Exception e) {
            // Covers expired, malformed, or bad-signature tokens — all treated as "not authenticated"
            return Optional.empty();
        }
    }

    /**
     * Validates a refresh token and extracts the user ID, for the /refresh endpoint.
     */
    public Optional<UUID> validateRefreshToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            if (!"refresh".equals(claims.get("type", String.class))) {
                return Optional.empty();
            }

            return Optional.of(UUID.fromString(claims.getSubject()));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}