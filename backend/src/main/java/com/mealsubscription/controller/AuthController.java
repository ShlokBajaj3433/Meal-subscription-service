package com.mealsubscription.controller;

import com.mealsubscription.dto.request.LoginRequest;
import com.mealsubscription.dto.request.RegisterRequest;
import com.mealsubscription.dto.response.AuthResponse;
import com.mealsubscription.dto.response.UserResponse;
import com.mealsubscription.security.JwtTokenProvider;
import com.mealsubscription.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return userService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        UserDetails principal = (UserDetails) auth.getPrincipal();
        String token = jwtTokenProvider.generateToken(auth);
        UserResponse user = userService.getByEmail(principal.getUsername());

        return AuthResponse.of(
            token,
            jwtTokenProvider.getExpirationMs(),
            user.id(),
            user.email(),
            user.role());
    }
}
