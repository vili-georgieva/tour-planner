package com.tourplanner.service;

import com.tourplanner.dto.AuthLoginRequestDto;
import com.tourplanner.dto.AuthRegisterRequestDto;
import com.tourplanner.dto.AuthResponseDto;
import com.tourplanner.exception.ServiceException;
import com.tourplanner.model.entity.UserEntity;
import com.tourplanner.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:authtest;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "tourplanner.jwt.secret=test-secret-key-minimum-32-chars-ok",
    "tourplanner.jwt.expiration-hours=1"
})
@Transactional
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void registerStoresPassword() {
        AuthRegisterRequestDto request = new AuthRegisterRequestDto("test-user", "Secret12");

        AuthResponseDto response = authService.register(request);

        assertNotNull(response.token());
        assertFalse(response.token().isBlank());
        assertEquals("test-user", response.username());

        // Verify password was actually stored in DB and hashed
        UserEntity savedUser = userRepository.findByUsername("test-user").orElseThrow();
        org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
        assertTrue(encoder.matches("Secret12", savedUser.getPasswordHash()));
    }

    @Test
    void registerRejectsDuplicateUsername() {
        AuthRegisterRequestDto first = new AuthRegisterRequestDto("duplicate", "Secret12");
        AuthRegisterRequestDto second = new AuthRegisterRequestDto("duplicate", "Another12");

        authService.register(first);

        ServiceException ex = assertThrows(ServiceException.class, () -> authService.register(second));
        assertTrue(ex.getMessage().toLowerCase().contains("already"));
    }

    @Test
    void loginReturnsUserOnValidCredentials() {
        AuthRegisterRequestDto registerRequest = new AuthRegisterRequestDto("login-user", "Secret12");
        authService.register(registerRequest);

        AuthLoginRequestDto loginRequest = new AuthLoginRequestDto("login-user", "Secret12");
        AuthResponseDto response = authService.login(loginRequest);

        assertNotNull(response.token());
        assertFalse(response.token().isBlank());
        assertEquals("login-user", response.username());
    }

    @Test
    void loginRejectsInvalidCredentials() {
        AuthRegisterRequestDto registerRequest = new AuthRegisterRequestDto("login-fail", "Secret12");
        authService.register(registerRequest);

        AuthLoginRequestDto loginRequest = new AuthLoginRequestDto("login-fail", "Wrong123");

        ServiceException ex = assertThrows(ServiceException.class, () -> authService.login(loginRequest));
        assertTrue(ex.getMessage().toLowerCase().contains("invalid"));
    }
}
