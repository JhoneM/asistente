package com.habitpet.services;

import com.habitpet.dtos.AuthResponse;
import com.habitpet.dtos.LoginRequest;
import com.habitpet.dtos.RegisterRequest;
import com.habitpet.exceptions.InvalidCredentialsException;
import com.habitpet.exceptions.ResourceAlreadyExistsException;
import com.habitpet.models.User;
import com.habitpet.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    // ============================================================================
    // register()
    // ============================================================================

    @Test
    void given_validData_when_register_then_returnsAuthResponseWithToken() {
        RegisterRequest request = new RegisterRequest(
                "juan@test.com", "password123", "Juan Perez", "America/Argentina/Buenos_Aires");

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hashed");
        when(jwtService.generateToken(anyString())).thenReturn("jwt-token");

        AuthResponse response = authService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.email()).isEqualTo("juan@test.com");
        assertThat(response.token()).isNotBlank();
        assertThat(response.userId()).isNotBlank();

        verify(userRepository).save(any(User.class));
        verify(jwtService).generateToken(anyString());
    }

    @Test
    void given_existingEmail_when_register_then_throwsResourceAlreadyExistsException() {
        RegisterRequest request = new RegisterRequest("juan@test.com", "password123", "Juan Perez", "UTC");

        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ResourceAlreadyExistsException.class);

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void given_nullTimezone_when_register_then_assignsUTC() {
        RegisterRequest request = new RegisterRequest("maria@test.com", "password456", "Maria Garcia", null);

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hashed");
        when(jwtService.generateToken(anyString())).thenReturn("jwt-token");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

        authService.register(request);

        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getTimezone()).isEqualTo("UTC");
    }

    // ============================================================================
    // login()
    // ============================================================================

    @Test
    void given_validCredentials_when_login_then_returnsAuthResponseWithToken() {
        LoginRequest request = new LoginRequest("juan@test.com", "password123");

        User user = new User();
        user.setId("user-123");
        user.setEmail("juan@test.com");
        user.setHashedPassword("$2a$10$hashed");
        user.setDisplayName("Juan Perez");

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getHashedPassword())).thenReturn(true);
        when(jwtService.generateToken(user.getId())).thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.email()).isEqualTo("juan@test.com");
        assertThat(response.token()).isNotBlank();
    }

    @Test
    void given_nonExistentEmail_when_login_then_throwsInvalidCredentialsException() {
        LoginRequest request = new LoginRequest("ghost@test.com", "password123");

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtService, never()).generateToken(anyString());
    }

    @Test
    void given_wrongPassword_when_login_then_throwsInvalidCredentialsException() {
        LoginRequest request = new LoginRequest("juan@test.com", "wrongPassword");

        User user = new User();
        user.setId("user-123");
        user.setEmail("juan@test.com");
        user.setHashedPassword("$2a$10$hashed");

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getHashedPassword())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(jwtService, never()).generateToken(anyString());
    }
}
