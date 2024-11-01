package org.achesnokov.demo.chat.model;

/**
 * Represents an authentication response containing a token.
 */
public class AuthResponse {
    private String token;

    /**
     * Gets the authentication token.
     *
     * @return the token.
     */
    public String getToken() {
        return token;
    }

    /**
     * Default constructor for AuthResponse.
     */
    public AuthResponse() {
    }

    /**
     * Constructs an AuthResponse with the given token.
     *
     * @param token the authentication token.
     */
    public AuthResponse(String token) {
        this.token = token;
    }
}
