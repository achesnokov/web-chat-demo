package org.achesnokov.demo.chat.controller;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.achesnokov.demo.chat.model.AuthRequest;
import org.achesnokov.demo.chat.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@QuarkusTest
public class AuthControllerTest {
    AutoCloseable autoCloseable;

    @InjectMock
    AuthService authService;
    @InjectMocks
    AuthController authController;

    @BeforeEach
    void setUp() {
        autoCloseable = openMocks(this);
    }

    void tierDown() throws Exception {
        autoCloseable.close();
    }

    @Test
    void testRegisterSuccessfully() {
        AuthRequest authRequest = new AuthRequest("newUser", "password123");
        String token = "dummy-jwt-token";

        when(authService.register(anyString(), anyString())).thenReturn(token);

        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(authRequest)
                .when().post("/api/auth/register")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body("token", is(token));
    }

    @Test
    void testRegisterFailsWithBadRequest() {
        AuthRequest authRequest = new AuthRequest("newUser", "password123");

        when(authService.register(anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("Username already exists"));

        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(authRequest)
                .when().post("/api/auth/register")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .body(is("Username already exists"));
    }

    @Test
    void testLoginSuccessfully() {
        AuthRequest authRequest = new AuthRequest("existingUser", "password123");
        String token = "dummy-jwt-token";

        when(authService.login(anyString(), anyString())).thenReturn(token);

        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(authRequest)
                .when().post("/api/auth/login")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body("token", is(token));
    }

    @Test
    void testLoginFailsWithUnauthorized() {
        AuthRequest authRequest = new AuthRequest("wrongUser", "wrongPassword");

        when(authService.login(anyString(), anyString())).thenThrow(new IllegalArgumentException("Invalid credentials"));

        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(authRequest)
                .when().post("/api/auth/login")
                .then()
                .statusCode(Response.Status.UNAUTHORIZED.getStatusCode())
                .body(is("Invalid credentials"));
    }

    @Test
    @TestSecurity(user = "user-123")
    void testValidateTokenSuccessfully() {
        given()
                .when().get("/api/auth/validate")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body(is("Token is valid"));
    }
}
