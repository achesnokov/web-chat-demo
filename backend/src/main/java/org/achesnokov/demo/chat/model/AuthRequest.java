package org.achesnokov.demo.chat.model;

/**
 * Represents an authentication request containing a username and password.
 */
public class AuthRequest {
    private String username;
    private String password;

    public AuthRequest() {
    }


    public AuthRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Gets the username for the authentication request.
     *
     * @return the username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username for the authentication request.
     *
     * @param username the username to set.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the password for the authentication request.
     *
     * @return the password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password for the authentication request.
     *
     * @param password the password to set.
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
