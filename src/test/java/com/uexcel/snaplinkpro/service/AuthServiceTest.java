package com.uexcel.snaplinkpro.service;


import com.uexcel.snaplinkpro.auth.dto.AuthResponse;
import com.uexcel.snaplinkpro.auth.dto.LoginRequest;
import com.uexcel.snaplinkpro.auth.dto.RegisterRequest;
import com.uexcel.snaplinkpro.auth.entity.Role;
import com.uexcel.snaplinkpro.auth.entity.User;
import com.uexcel.snaplinkpro.auth.repository.UserRepository;
import com.uexcel.snaplinkpro.auth.service.AuthService;
import com.uexcel.snaplinkpro.exception.EmailAlreadyExistsException;
import com.uexcel.snaplinkpro.exception.UserNotFoundException;
import com.uexcel.snaplinkpro.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Test
    void shouldRegisterUserSuccessfully() {

        RegisterRequest request = new RegisterRequest();
        request.setName("Udoka");
        request.setEmail("test@example.com");
        request.setPassword("password");

        when(userRepository.existsByEmail("test@example.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("password"))
                .thenReturn("encodedPassword");

        when(jwtService.generateToken(any()))
                .thenReturn("jwt-token");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("USER", response.getRole());

        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("password");
        verify(jwtService).generateToken(any());
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {

        RegisterRequest request = new RegisterRequest();
        request.setName("Udoka");
        request.setEmail("test@example.com");
        request.setPassword("password");

        when(userRepository.existsByEmail("test@example.com"))
                .thenReturn(true);

        assertThrows(
                EmailAlreadyExistsException.class,
                () -> authService.register(request)
        );

        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void shouldLoginUserSuccessfully() {

        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password");

        User user = User.builder()
                .id(1L)
                .name("Udoka")
                .email("test@example.com")
                .password("encodedPassword")
                .role(Role.USER)
                .build();

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        when(jwtService.generateToken(any()))
                .thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("USER", response.getRole());

        verify(authenticationManager).authenticate(any());
        verify(userRepository).findByEmail("test@example.com");
        verify(jwtService).generateToken(any());
    }

    @Test
    void shouldThrowUserNotFoundWhenLoginUserDoesNotExist() {

        LoginRequest request = new LoginRequest();
        request.setEmail("missing@example.com");
        request.setPassword("password");

        when(userRepository.findByEmail("missing@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> authService.login(request)
        );

        verify(authenticationManager).authenticate(any());
        verify(jwtService, never()).generateToken(any());
    }
}
