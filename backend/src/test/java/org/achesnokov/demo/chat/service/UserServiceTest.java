package org.achesnokov.demo.chat.service;

import java.util.Optional;
import java.util.UUID;

import org.achesnokov.demo.chat.model.User;
import org.achesnokov.demo.chat.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @InjectMocks
    UserService userService;

    @Mock
    UserRepository userRepository;

    @BeforeEach
    void setUp() {
        // Setup if necessary
    }

    @Test
    void createUserCreatesNewUser() {
        String userName = "testUser";

        doNothing().when(userRepository).save(any(User.class));

        User result = userService.createUser(userName);

        assertNotNull(result, "User should be created");
        assertEquals(userName, result.getUsername(), "Username should match");
        assertNotNull(result.getUserId(), "User ID should be generated");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createUserGeneratesUniqueId() {
        String userName = "testUser";

        doNothing().when(userRepository).save(any(User.class));

        User firstUser = userService.createUser(userName);
        User secondUser = userService.createUser(userName);

        assertNotEquals(firstUser.getUserId(), secondUser.getUserId(), "User IDs should be unique");
    }

    @Test
    void getUserByIdReturnsUserIfExists() {
        String userId = UUID.randomUUID().toString();
        User user = new User();
        user.setUserId(userId);
        user.setUsername("testUser");

        when(userRepository.findById(userId)).thenReturn(user);

        Optional<User> result = userService.getUserById(userId);

        assertTrue(result.isPresent(), "User should be found");
        assertEquals(userId, result.get().getUserId(), "User ID should match");
    }

    @Test
    void getUserByIdReturnsEmptyIfNotExists() {
        String userId = UUID.randomUUID().toString();

        when(userRepository.findById(userId)).thenReturn(null);

        Optional<User> result = userService.getUserById(userId);

        assertFalse(result.isPresent(), "User should not be found");
    }

    @Test
    void deleteUserDeletesUserSuccessfully() {
        String userId = UUID.randomUUID().toString();

        doNothing().when(userRepository).delete(userId);

        userService.deleteUser(userId);

        verify(userRepository, times(1)).delete(userId);
    }

    @Test
    void updateUserNameUpdatesUserSuccessfully() {
        String userId = UUID.randomUUID().toString();
        String newUserName = "updatedUser";

        User user = new User();
        user.setUserId(userId);
        user.setUsername("oldUser");

        when(userRepository.findById(userId)).thenReturn(user);
        doNothing().when(userRepository).save(user);

        userService.updateUserName(userId, newUserName);

        assertEquals(newUserName, user.getUsername(), "Username should be updated");
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void updateUserNameThrowsExceptionIfUserNotFound() {
        String userId = UUID.randomUUID().toString();
        String newUserName = "updatedUser";

        when(userRepository.findById(userId)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> userService.updateUserName(userId, newUserName), "Should throw exception if user not found");
    }

    // New tests for nullable arguments
    @Test
    void createUserThrowsExceptionIfUserNameIsNull() {
        String userName = null;

        assertThrows(NullPointerException.class, () -> userService.createUser(userName), "Should throw exception if username is null");
    }

    @Test
    void getUserByIdThrowsExceptionIfUserIdIsNull() {
        String userId = null;

        assertThrows(NullPointerException.class, () -> userService.getUserById(userId), "Should throw exception if userId is null");
    }

    @Test
    void updateUserNameThrowsExceptionIfUserIdIsNull() {
        String userId = null;
        String newUserName = "updatedUser";

        assertThrows(NullPointerException.class, () -> userService.updateUserName(userId, newUserName), "Should throw exception if userId is null");
    }

    @Test
    void updateUserNameThrowsExceptionIfNewUserNameIsNull() {
        String userId = UUID.randomUUID().toString();
        String newUserName = null;

        assertThrows(NullPointerException.class, () -> userService.updateUserName(userId, newUserName), "Should throw exception if new username is null");
    }
}
