package com.mealsubscription.service.impl;

import com.mealsubscription.dto.request.RegisterRequest;
import com.mealsubscription.dto.request.UpdateProfileRequest;
import com.mealsubscription.dto.response.UserResponse;
import com.mealsubscription.entity.User;
import com.mealsubscription.exception.DuplicateEmailException;
import com.mealsubscription.exception.ResourceNotFoundException;
import com.mealsubscription.repository.UserRepository;
import com.mealsubscription.service.UserService;
import com.mealsubscription.util.EntityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EntityMapper mapper;

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException(request.email());
        }

        User user = User.builder()
            .name(request.name())
            .email(request.email().toLowerCase().trim())
            .passwordHash(passwordEncoder.encode(request.password()))
            .build();

        User saved = userRepository.save(user);
        log.info("Registered new user: id={}, email={}", saved.getId(), saved.getEmail());
        return mapper.toUserResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        return mapper.toUserResponse(findUserById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getByEmail(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> ResourceNotFoundException.forEntity("User", null));
        return mapper.toUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = findUserById(userId);

        if (StringUtils.hasText(request.name())) {
            user.setName(request.name());
        }

        if (StringUtils.hasText(request.newPassword())) {
            if (!StringUtils.hasText(request.currentPassword()) ||
                !passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
                throw new IllegalArgumentException("Current password is incorrect");
            }
            user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
            log.info("Password changed for user id={}", userId);
        }

        return mapper.toUserResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void deactivate(Long userId) {
        User user = findUserById(userId);
        user.setActive(false);
        userRepository.save(user);
        log.warn("User deactivated: id={}, email={}", userId, user.getEmail());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> listAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(mapper::toUserResponse);
    }

    // ── Shared helper ────────────────────────────────────────────────────────

    private User findUserById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> ResourceNotFoundException.forEntity("User", id));
    }
}
