package org.achesnokov.demo.chat.service;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.achesnokov.demo.chat.model.User;
import org.achesnokov.demo.chat.repository.UserRepository;

/**
 * Service class for managing user operations.
 */
@ApplicationScoped
public class UserService {

    private final UserRepository userRepository;

    /**
     * Constructor for UserService.
     *
     * @param userRepository the user repository
     */
    @Inject
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Creates a new user.
     *
     * @param userName the username of the new user
     * @return the created User object
     */
    public User createUser(String userName) {
        Objects.requireNonNull(userName, "Username must not be null");

        String userId = UUID.randomUUID().toString();
        User user = new User();
        user.setUserId(userId);
        user.setUsername(userName);
        userRepository.save(user);

        return user;
    }

    /**
     * Retrieves a user by their ID.
     *
     * @param userId the ID of the user
     * @return an Optional containing the User if found, or empty if not found
     */
    public Optional<User> getUserById(String userId) {
        Objects.requireNonNull(userId, "User ID must not be null");

        User user = userRepository.findById(userId);
        return Optional.ofNullable(user);
    }

    /**
     * Deletes a user by their ID.
     *
     * @param userId the ID of the user to delete
     */
    public void deleteUser(String userId) {
        userRepository.delete(userId);
    }

    /**
     * Updates the username of a user.
     *
     * @param userId the ID of the user
     * @param newUserName the new username
     * @throws IllegalArgumentException if the user is not found
     */
    public void updateUserName(String userId, String newUserName) {
        Objects.requireNonNull(userId, "User ID must not be null");
        Objects.requireNonNull(newUserName, "New username must not be null");

        User user = userRepository.findById(userId);
        if (user == null) {
            //TODO: replace it with application specific exception
            throw new IllegalArgumentException("User not found: " + userId);
        }
        user.setUsername(newUserName);
        userRepository.save(user);
    }
}