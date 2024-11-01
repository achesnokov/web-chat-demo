package org.achesnokov.demo.chat.service;

import java.util.Optional;

import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import org.achesnokov.demo.chat.model.User;
import org.achesnokov.demo.chat.repository.UserRepository;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @InjectMocks
    AuthService authService;

    String issuer = "issuer";

    @Mock
    UserRepository userRepository;

    @Mock
    JsonWebToken jsonWebToken;

    @Mock
    JWTParser parser;



    @Test
    void registerCreatesNewUserAndReturnsToken() {
        when(userRepository.findByUsername(any())).thenReturn(Optional.empty());
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setUserId("test-user-id");
            return null;
        }).when(userRepository).save(any(User.class));

        String token = authService.register("testuser", "password123");
        assertNotNull(token, "Token should not be null");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void registerThrowsExceptionIfUserExists() {
        when(userRepository.findByUsername(any())).thenReturn(Optional.of(new User()));

        assertThrows(IllegalArgumentException.class, () -> authService.register("existingUser", "password123"), "Should throw exception if user already exists");
    }

    @Test
    void validateTokenReturnsUserIfTokenIsValid() throws ParseException {
        authService.issuer = issuer;
        String validToken = "valid-token";
        when(parser.parse(validToken)).thenReturn(jsonWebToken);
        when(jsonWebToken.getIssuer()).thenReturn(issuer);
        when(jsonWebToken.getSubject()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(new User()));

        Optional<User> result = authService.validateToken(validToken);
        assertTrue(result.isPresent(), "User should be present if token is valid");
    }

    @Test
    void validateTokenReturnsEmptyIfTokenIsInvalid() throws ParseException {
        String invalidToken = "invalid-token";
        when(parser.parse(invalidToken)).thenThrow(new RuntimeException("Invalid token"));

        Optional<User> result = authService.validateToken(invalidToken);
        assertFalse(result.isPresent(), "User should not be present if token is invalid");
    }

    @Test
    void generateJwtTokenReturnsValidToken() {
        String token = authService.generateJwtToken("testuser");
        assertNotNull(token, "Generated token should not be null");
    }

    @Test
    void checkPasswordReturnsTrueForMatchingPasswords() {
        String plainPassword = "password123";
        String hashedPassword = authService.hashPassword(plainPassword);

        assertTrue(authService.checkPassword(plainPassword, hashedPassword), "Passwords should match");
    }

    @Test
    void checkPasswordReturnsFalseForNonMatchingPasswords() {
        String plainPassword = "password123";
        String hashedPassword = authService.hashPassword("differentPassword");

        assertFalse(authService.checkPassword(plainPassword, hashedPassword), "Passwords should not match");
    }

    @Test
    void hashPasswordGeneratesNonNullHash() {
        String hashedPassword = authService.hashPassword("password123");
        assertNotNull(hashedPassword, "Hashed password should not be null");
    }
}
