package com.mealsubscription.service;

import com.mealsubscription.dto.request.RegisterRequest;
import com.mealsubscription.dto.response.UserResponse;
import com.mealsubscription.entity.Role;
import com.mealsubscription.entity.User;
import com.mealsubscription.exception.DuplicateEmailException;
import com.mealsubscription.repository.UserRepository;
import com.mealsubscription.service.impl.UserServiceImpl;
import com.mealsubscription.util.EntityMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private EntityMapper mapper;

    @InjectMocks
    private UserServiceImpl userService;

    private RegisterRequest validRequest;
    private User savedUser;
    private UserResponse expectedResponse;

    @BeforeEach
    void setUp() {
        validRequest = new RegisterRequest("John Doe", "john@example.com", "Password1!");
        savedUser = User.builder()
            .id(1L).name("John Doe").email("john@example.com")
            .passwordHash("hashed").role(Role.USER).build();
        expectedResponse = new UserResponse(1L, "John Doe", "john@example.com",
            Role.USER, true, LocalDateTime.now());
    }

    @Test
    @DisplayName("register() — success path: saves user and returns response DTO")
    void register_success() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(mapper.toUserResponse(savedUser)).thenReturn(expectedResponse);

        UserResponse result = userService.register(validRequest);

        assertThat(result.email()).isEqualTo("john@example.com");
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("Password1!");
    }

    @Test
    @DisplayName("register() — duplicate email throws DuplicateEmailException")
    void register_duplicateEmail_throws() {
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(validRequest))
            .isInstanceOf(DuplicateEmailException.class)
            .hasMessageContaining("john@example.com");

        verify(userRepository, never()).save(any());
    }
}
