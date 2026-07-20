package com.credx.dispatchhub.service;

import com.credx.dispatchhub.dto.request.LoginRequest;
import com.credx.dispatchhub.dto.request.RegisterRequest;
import com.credx.dispatchhub.dto.response.AuthResponse;
import com.credx.dispatchhub.dto.response.UserResponse;
import com.credx.dispatchhub.entity.DriverProfile;
import com.credx.dispatchhub.entity.RiderProfile;
import com.credx.dispatchhub.entity.User;
import com.credx.dispatchhub.enums.DriverStatus;
import com.credx.dispatchhub.enums.UserRole;
import com.credx.dispatchhub.exception.DuplicateResourceException;
import com.credx.dispatchhub.exception.InvalidCredentialsException;
import com.credx.dispatchhub.repository.DriverProfileRepository;
import com.credx.dispatchhub.repository.RiderProfileRepository;
import com.credx.dispatchhub.repository.UserRepository;
import com.credx.dispatchhub.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final DriverProfileRepository driverProfileRepository;
    private final RiderProfileRepository riderProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (request.role() != UserRole.RIDER && request.role() != UserRole.DRIVER) {
            throw new IllegalArgumentException("Public registration is only available for rider and driver accounts");
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("An account with this email already exists");
        }

        User user = User.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .fullName(request.fullName())
                .phoneNumber(request.phoneNumber())
                .role(request.role())
                .enabled(true)
                .build();

        user = userRepository.save(user);

        if (user.getRole() == UserRole.DRIVER) {
            DriverProfile driverProfile = DriverProfile.builder()
                    .user(user)
                    .vehicleMake("Unspecified")
                    .vehicleModel("Unspecified")
                    .licensePlate("PENDING-" + user.getId())
                    .status(DriverStatus.OFFLINE)
                    .build();
            driverProfileRepository.save(driverProfile);
        } else if (user.getRole() == UserRole.RIDER) {
            RiderProfile riderProfile = RiderProfile.builder()
                    .user(user)
                    .build();
            riderProfileRepository.save(riderProfile);
        }

        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        if (!user.isEnabled()) {
            throw new InvalidCredentialsException("This account has been disabled");
        }

        return buildAuthResponse(user);
    }

    /**
     * Exchanges a valid refresh token for a new access + refresh token pair.
     * The presented refresh token is single-use: it is revoked here and a
     * replacement is issued (rotation).
     */
    @Transactional
    public AuthResponse refresh(String refreshToken) {
        User user = refreshTokenService.validateAndRevoke(refreshToken);
        return buildAuthResponse(user);
    }

    /** Revokes all refresh tokens for the user; the access token simply expires. */
    @Transactional
    public void logout(Long userId) {
        refreshTokenService.revokeAllForUser(userId);
    }

    private AuthResponse buildAuthResponse(User user) {
        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresInMs(jwtService.getExpirationMs())
                .refreshToken(refreshTokenService.issue(user))
                .refreshExpiresInMs(refreshTokenService.getRefreshExpirationMs())
                .user(toUserResponse(user))
                .build();
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .build();
    }
}
