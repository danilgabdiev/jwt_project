package ru.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.auth.dto.*;
import ru.auth.entity.*;
import ru.auth.exception.*;
import ru.auth.repository.*;
import ru.auth.security.JwtService;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final RevokedTokenRepository revokedRepo;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest req) {
        if (userRepo.existsByUsername(req.username())) {
            throw new UserAlreadyExistsException("Username already taken");
        }
        if (userRepo.existsByEmail(req.email())) {
            throw new UserAlreadyExistsException("Email already taken");
        }

        Set<Role> roles = Optional.ofNullable(req.roles())
                .filter(r -> !r.isEmpty())
                .orElse(Set.of("ROLE_GUEST"))
                .stream()
                .map(name -> roleRepo.findByName(name)
                        .orElseThrow(() -> new RoleNotFoundException("Invalid role: " + name)))
                .collect(Collectors.toSet());

        User user = User.builder()
                .username(req.username())
                .email(req.email())
                .password(passwordEncoder.encode(req.password()))
                .roles(roles)
                .build();
        userRepo.save(user);

        UserDetails ud = mapToUserDetails(user);
        String access = jwtService.generateToken(ud);
        String refresh = jwtService.generateRefreshToken(ud);
        return new AuthResponse(access, refresh);
    }

    public AuthResponse login(LoginRequest req) {
        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.username(), req.password())
            );
        } catch (BadCredentialsException ex) {
            throw new InvalidCredentialsException("Invalid username or password");
        }
        User user = userRepo.findByUsername(req.username())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        UserDetails ud = mapToUserDetails(user);
        String access = jwtService.generateToken(ud);
        String refresh = jwtService.generateRefreshToken(ud);
        return new AuthResponse(access, refresh);
    }

    public AuthResponse refreshToken(RefreshTokenRequest req) {
        String token = req.refreshToken();
        if (!"refresh".equals(jwtService.extractTokenType(token))) {
            throw new InvalidTokenException("Not a refresh token");
        }
        if (revokedRepo.existsByToken(token)) {
            throw new InvalidTokenException("Token revoked");
        }
        if (!jwtService.isTokenValid(token)) {
            throw new InvalidTokenException("Token invalid or expired");
        }
        String username = jwtService.extractUsername(token);
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        String access = jwtService.generateToken(mapToUserDetails(user));
        return new AuthResponse(access, token);
    }

    public void logout(RefreshTokenRequest req) {
        String token = req.refreshToken();
        if (!"refresh".equals(jwtService.extractTokenType(token))) {
            throw new InvalidTokenException("Only refresh tokens can be revoked");
        }
        Instant exp = jwtService.extractExpiration(token);
        if (!revokedRepo.existsByToken(token)) {
            revokedRepo.save(RevokedToken.builder()
                    .token(token)
                    .expiryDate(exp)
                    .build());
        }
    }

    private UserDetails mapToUserDetails(User user) {
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(
                        user.getRoles().stream()
                                .map(Role::getName)
                                .toArray(String[]::new)
                )
                .build();
    }
}
