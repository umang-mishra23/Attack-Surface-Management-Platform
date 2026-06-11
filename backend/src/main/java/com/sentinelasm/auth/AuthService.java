package com.sentinelasm.auth;

import com.sentinelasm.security.JwtService;
import com.sentinelasm.user.AppUser;
import com.sentinelasm.user.UserRepository;
import com.sentinelasm.user.UserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;

@Service
public class AuthService {
    private final UserRepository users;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository users, UserService userService, PasswordEncoder passwordEncoder,
                       JwtService jwtService, AuthenticationManager authenticationManager) {
        this.users = users;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest request) {
        if (users.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email is already registered");
        }
        AppUser user = new AppUser();
        user.setName(request.name());
        user.setEmail(request.email().toLowerCase());
        user.setCompany(request.company());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setApiKey(generateApiKey());
        users.save(user);
        return response(user);
    }

    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        return response(userService.loadUserByUsername(request.email()));
    }

    public AuthDtos.UserProfile profile() {
        return profile(userService.currentUser());
    }

    public void changePassword(AuthDtos.ChangePasswordRequest request) {
        AppUser user = userService.currentUser();
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is invalid");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        users.save(user);
    }

    private AuthDtos.AuthResponse response(AppUser user) {
        return new AuthDtos.AuthResponse(jwtService.generate(user), profile(user));
    }

    private AuthDtos.UserProfile profile(AppUser user) {
        return new AuthDtos.UserProfile(
                user.getId().toString(),
                user.getName(),
                user.getEmail(),
                user.getCompany(),
                user.getRole(),
                user.getApiKey()
        );
    }

    private String generateApiKey() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return "asm_" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
