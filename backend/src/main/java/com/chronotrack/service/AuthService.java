package com.chronotrack.service;

import com.chronotrack.dto.*;
import com.chronotrack.entity.User;
import com.chronotrack.entity.UserSettings;
import com.chronotrack.exception.BadRequestException;
import com.chronotrack.exception.DuplicateResourceException;
import com.chronotrack.exception.ResourceNotFoundException;
import com.chronotrack.repository.UserRepository;
import com.chronotrack.repository.UserSettingsRepository;
import com.chronotrack.security.CustomUserDetails;
import com.chronotrack.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Service managing user registration, authentication, token issuance, and profile configuration.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final CategoryService categoryService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserRepository userRepository,
                       UserSettingsRepository userSettingsRepository,
                       CategoryService categoryService,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.userSettingsRepository = userSettingsRepository;
        this.categoryService = categoryService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    /**
     * Registers a new user, hashes password, saves settings, seeds default categories, and returns JWT.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException("An account with email '" + email + "' already exists.");
        }

        User user = new User();
        user.setFullName(request.getFullName().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        User savedUser = userRepository.save(user);

        // Create default user settings
        String timezone = StringUtils.hasText(request.getTimezone()) ? request.getTimezone().trim() : "UTC";
        String preferredFormat = StringUtils.hasText(request.getPreferredTimeFormat()) ? request.getPreferredTimeFormat().trim() : "24h";
        UserSettings settings = new UserSettings(savedUser, timezone, preferredFormat);
        userSettingsRepository.save(settings);

        // Seed default categories
        categoryService.seedDefaultCategoriesForUser(savedUser);

        // Generate JWT token
        CustomUserDetails userDetails = CustomUserDetails.build(savedUser);
        String token = tokenProvider.generateToken(userDetails);

        return new AuthResponse(
                token,
                savedUser.getId(),
                savedUser.getFullName(),
                savedUser.getEmail(),
                settings.getTimezone(),
                settings.getPreferredTimeFormat()
        );
    }

    /**
     * Authenticates user credentials via Spring Security and returns JWT token and profile details.
     */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword())
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String token = tokenProvider.generateToken(userDetails);

        UserSettings settings = userSettingsRepository.findByUserId(userDetails.getId())
                .orElse(new UserSettings(null, "UTC", "24h"));

        return new AuthResponse(
                token,
                userDetails.getId(),
                userDetails.getFullName(),
                userDetails.getUsername(),
                settings.getTimezone(),
                settings.getPreferredTimeFormat()
        );
    }

    /**
     * Fetches current authenticated user's profile and regional settings.
     */
    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        UserSettings settings = userSettingsRepository.findByUserId(userId)
                .orElse(new UserSettings(user, "UTC", "24h"));

        return new UserProfileResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                settings.getTimezone(),
                settings.getPreferredTimeFormat(),
                user.getCreatedAt()
        );
    }

    /**
     * Updates full name, password (if requested with old password validation), and regional preferences.
     */
    @Transactional
    public UserProfileResponse updateUserProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (StringUtils.hasText(request.getFullName())) {
            user.setFullName(request.getFullName().trim());
        }

        // Handle password change if requested
        if (StringUtils.hasText(request.getNewPassword())) {
            if (!StringUtils.hasText(request.getCurrentPassword())) {
                throw new BadRequestException("Current password is required to set a new password.");
            }
            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                throw new BadRequestException("Current password does not match.");
            }
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        }

        userRepository.save(user);

        // Update settings
        UserSettings settings = userSettingsRepository.findByUserId(userId)
                .orElseGet(() -> new UserSettings(user, "UTC", "24h"));

        if (StringUtils.hasText(request.getTimezone())) {
            settings.setTimezone(request.getTimezone().trim());
        }
        if (StringUtils.hasText(request.getPreferredTimeFormat())) {
            settings.setPreferredTimeFormat(request.getPreferredTimeFormat().trim());
        }
        UserSettings savedSettings = userSettingsRepository.save(settings);

        return new UserProfileResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                savedSettings.getTimezone(),
                savedSettings.getPreferredTimeFormat(),
                user.getCreatedAt()
        );
    }
}
