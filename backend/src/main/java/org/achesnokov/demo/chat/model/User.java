package org.achesnokov.demo.chat.model;

import java.util.List;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

/**
 * Represents a user in the chat application, including their active chats.
 */
@DynamoDbBean
public class User {

    private String userId;
    private String username;
    private String password;
    private List<String> activeChatIds;

    /**
     * Gets the unique identifier for the user.
     *
     * @return the user ID.
     */
    @DynamoDbPartitionKey
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the unique identifier for the user.
     *
     * @param userId the user ID to set.
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Gets the username of the user.
     *
     * @return the username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username of the user.
     *
     * @param username the username to set.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the password of the user.
     *
     * @return the password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password of the user.
     *
     * @param password the password to set.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets the list of active chat IDs that the user is currently participating in.
     *
     * @return the list of active chat IDs.
     */
    public List<String> getActiveChatIds() {
        return activeChatIds;
    }

    /**
     * Sets the list of active chat IDs that the user is currently participating in.
     *
     * @param activeChatIds the list of active chat IDs to set.
     */
    public void setActiveChatIds(List<String> activeChatIds) {
        this.activeChatIds = activeChatIds;
    }
}
