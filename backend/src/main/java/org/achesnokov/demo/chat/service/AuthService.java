package org.achesnokov.demo.chat.service;

import java.security.Principal;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.achesnokov.demo.chat.model.User;
import org.achesnokov.demo.chat.repository.UserRepository;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Service class for handling authentication-related operations.
 */
@ApplicationScoped
public class AuthService {
    private static final Logger LOGGER = Logger.getLogger(AuthService.class);

    @Inject
    @ConfigProperty(name = "smallrye.jwt.issuer")
    String issuer;

    @Inject
    SecurityIdentity securityIdentity;

    @Inject
    JWTParser parser;

    @Inject
    UserRepository userRepository;

    private static final long EXPIRATION_TIME = 86400000;

    /**
     * Registers a new user with the given username and password.
     *
     * @param username the username of the new user
     * @param password the password of the new user
     * @return a JWT token for the newly registered user
     * @throws IllegalArgumentException if the user already exists
     */
    public String register(String username, String password) {
        Optional<User> existingUser = userRepository.findByUsername(username);
        if (existingUser.isPresent()) {
            throw new IllegalArgumentException("User already exists");
        }

        String hashedPassword = hashPassword(password);

        User user = new User();
        user.setUserId(UUID.randomUUID().toString());
        user.setUsername(username);
        user.setPassword(hashedPassword);
        userRepository.save(user);

        return generateJwtToken(username);
    }

    /**
     * Logs in a user with the given username and password.
     *
     * @param username the username of the user
     * @param password the password of the user
     * @return a JWT token for the logged-in user
     * @throws IllegalArgumentException if the username or password is invalid
     */
    public String login(String username, String password) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        User user = userOptional.get();

        if(!checkPassword(password, user.getPassword())){
            throw new IllegalArgumentException("Invalid username or password");
        }

        return generateJwtToken(username);
    }

    /**
     * Retrieves the currently authenticated user.
     *
     * @return an Optional containing the current user, or an empty Optional if no user is authenticated
     */
    public Optional<User> getCurrentUser() {
        return Optional
                .ofNullable(securityIdentity.getPrincipal())
                .map(Principal::getName)
                .flatMap(username -> userRepository.findByUsername(username));
    }

    /**
     * Validates a JWT token and retrieves the associated user.
     *
     * @param token the JWT token to validate
     * @return an Optional containing the user associated with the token, or an empty Optional if the token is invalid
     */
    public Optional<User> validateToken(String token) {
        try {
            JsonWebToken jwt = parser.parse(token);

            if (!issuer.equals(jwt.getIssuer())) {
                LOGGER.error("Invalid token issuer");
                return Optional.empty();
            }

            String username = jwt.getSubject();
            if (username == null || username.isEmpty()) {
                LOGGER.error("No username in token");
                return Optional.empty();
            }

            return userRepository.findByUsername(username);

        } catch (Exception e) {
            LOGGER.error("Failed to validate JWT token", e);
            return Optional.empty();
        }
    }

    /**
     * Hashes a password using BCrypt.
     *
     * @param password the password to hash
     * @return the hashed password
     */
    protected String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    /**
     * Generates a JWT token for the given username.
     *
     * @param username the username for which to generate the token
     * @return the generated JWT token
     */
    public String generateJwtToken(String username) {
        String token = Jwt
                .issuer(issuer)
                .subject(username)
                .upn(username)
                .expiresIn(Duration.ofHours(1))
                .sign();

        return token;
    }

    /**
     * Checks if a plain password matches a hashed password.
     *
     * @param plainPassword the plain password
     * @param hashedPassword the hashed password
     * @return true if the passwords match, false otherwise
     */
    public boolean checkPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}