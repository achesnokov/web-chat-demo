package org.achesnokov.demo.chat.model;

import java.time.Instant;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

/**
 * Represents a participant in a chat, including information about when they joined and left.
 */
@DynamoDbBean
public class ChatParticipant {

    private String chatId;
    private String userId;
    private Instant joinedAt;
    private Instant leftAt;

    /**
     * Gets the chat ID to which the participant belongs.
     *
     * @return the chat ID.
     */
    @DynamoDbPartitionKey
    public String getChatId() {
        return chatId;
    }

    /**
     * Sets the chat ID to which the participant belongs.
     *
     * @param chatId the chat ID to set.
     */
    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    /**
     * Gets the user ID of the participant.
     *
     * @return the user ID.
     */
    @DynamoDbSortKey
    @DynamoDbSecondaryPartitionKey(indexNames = "userId-index")
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the user ID of the participant.
     *
     * @param userId the user ID to set.
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Gets the timestamp when the participant joined the chat.
     *
     * @return the join timestamp.
     */
    public Instant getJoinedAt() {
        return joinedAt;
    }

    /**
     * Sets the timestamp when the participant joined the chat.
     *
     * @param joinedAt the join timestamp to set.
     */
    public void setJoinedAt(Instant joinedAt) {
        this.joinedAt = joinedAt;
    }

    /**
     * Gets the timestamp when the participant left the chat.
     *
     * @return the leave timestamp.
     */
    public Instant getLeftAt() {
        return leftAt;
    }

    /**
     * Sets the timestamp when the participant left the chat.
     *
     * @param leftAt the leave timestamp to set.
     */
    public void setLeftAt(Instant leftAt) {
        this.leftAt = leftAt;
    }
}
