package com.tourplanner.service;

import com.tourplanner.config.JwtService;
import com.tourplanner.dto.AuthLoginRequestDto;
import com.tourplanner.dto.AuthRegisterRequestDto;
import com.tourplanner.dto.AuthResponseDto;
import com.tourplanner.exception.ServiceException;
import com.tourplanner.model.entity.UserEntity;
import com.tourplanner.repository.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Validated
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public AuthResponseDto register(@Valid @NotNull AuthRegisterRequestDto request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ServiceException("Username already exists.");
        }

        UserEntity user = new UserEntity();
        user.setUsername(request.username());
        user.setPasswordHash(passwordEncoder.encode(request.password()));

        UserEntity savedUser = userRepository.save(user);
        String token = jwtService.generateToken(savedUser.getId());
        return new AuthResponseDto(token, savedUser.getUsername());
    }

    public AuthResponseDto login(@Valid @NotNull AuthLoginRequestDto request) {
        UserEntity user = userRepository.findByUsername(request.username())
            .orElseThrow(() -> new ServiceException("Invalid username or password."));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ServiceException("Invalid username or password.");
        }

        String token = jwtService.generateToken(user.getId());
        return new AuthResponseDto(token, user.getUsername());
    }

    public boolean userExists(@NotNull Long userId) {
        return userRepository.existsById(userId);
    }
}
