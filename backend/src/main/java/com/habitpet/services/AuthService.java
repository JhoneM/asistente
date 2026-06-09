package com.habitpet.services;

import com.habitpet.dtos.AuthResponse;
import com.habitpet.dtos.LoginRequest;
import com.habitpet.dtos.RegisterRequest;
import com.habitpet.exceptions.InvalidCredentialsException;
import com.habitpet.exceptions.ResourceAlreadyExistsException;
import com.habitpet.models.User;
import com.habitpet.repositories.PetRepository;
import com.habitpet.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PetRepository petRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final PetFactory petFactory;

    /**
     * Registers a new user in the system.
     *
     * @param request registration data
     * @return JWT token and basic user data
     * @throws ResourceAlreadyExistsException if the email is already registered
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ResourceAlreadyExistsException(
                "Email already registered",
                "Registration attempt with existing email: " + request.email()
            );
        }

        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setEmail(request.email());
        user.setHashedPassword(passwordEncoder.encode(request.password()));
        user.setDisplayName(request.displayName());
        user.setTimezone(request.timezone());

        userRepository.save(user);
        petRepository.save(petFactory.createDefaultFor(user));
        log.info("User registered: userId={}", user.getId());

        String token = jwtService.generateToken(user.getId());
        return new AuthResponse(token, user.getId(), user.getEmail(), user.getDisplayName());
    }

    /**
     * Authenticates a user with email and password.
     *
     * @param request login credentials
     * @return JWT token and basic user data
     * @throws InvalidCredentialsException if the email does not exist or the password is incorrect
     */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getHashedPassword())) {
            throw new InvalidCredentialsException();
        }

        log.info("Login successful: userId={}", user.getId());

        String token = jwtService.generateToken(user.getId());
        return new AuthResponse(token, user.getId(), user.getEmail(), user.getDisplayName());
    }
}
