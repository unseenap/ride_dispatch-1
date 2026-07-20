package com.credx.dispatchhub.service;

import com.credx.dispatchhub.entity.RefreshToken;
import com.credx.dispatchhub.entity.User;
import com.credx.dispatchhub.exception.InvalidCredentialsException;
import com.credx.dispatchhub.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${dispatchhub.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    /** Issues a new opaque refresh token for the user and returns its plaintext value. */
    @Transactional
    public String issue(User user) {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        refreshTokenRepository.save(RefreshToken.builder()
                .user(user)
                .tokenHash(hash(token))
                .expiresAt(Instant.now().plusMillis(refreshExpirationMs))
                .build());

        return token;
    }

    /**
     * Validates the presented token and rotates it: the old token is revoked
     * and the caller should issue a fresh access + refresh token pair.
     * Returns the owning user.
     */
    @Transactional
    public User validateAndRevoke(String token) {
        RefreshToken stored = refreshTokenRepository.findByTokenHash(hash(token))
                .orElseThrow(() -> new InvalidCredentialsException("Invalid refresh token"));

        if (stored.isRevoked()) {
            // Reuse of a rotated token suggests it was stolen; revoke the whole
            // family so neither the attacker nor the victim keeps a session.
            refreshTokenRepository.revokeAllForUser(stored.getUser().getId());
            throw new InvalidCredentialsException("Refresh token has been revoked");
        }
        if (stored.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidCredentialsException("Refresh token has expired");
        }
        if (!stored.getUser().isEnabled()) {
            throw new InvalidCredentialsException("This account has been disabled");
        }

        stored.setRevoked(true);
        return stored.getUser();
    }

    /** Revokes every active refresh token for the user (logout everywhere). */
    @Transactional
    public void revokeAllForUser(Long userId) {
        refreshTokenRepository.revokeAllForUser(userId);
    }

    public long getRefreshExpirationMs() {
        return refreshExpirationMs;
    }

    private String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
