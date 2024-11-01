package org.achesnokov.demo.chat.model;

import java.time.Instant;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

/**
 * Represents a message in a chat, including the sender, content, and timestamp.
 */
@DynamoDbBean
public class Message {

    private String chatId;
    private String messageId;
    private String userId;
    private String content;
    private Instant timestamp;

    /**
     * Gets the chat ID to which this message belongs.
     *
     * @return the chat ID.
     */
    @DynamoDbPartitionKey
    public String getChatId() {
        return chatId;
    }

    /**
     * Sets the chat ID to which this message belongs.
     *
     * @param chatId the chat ID to set.
     */
    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    /**
     * Gets the unique identifier of this message.
     *
     * @return the message ID.
     */
    public String getMessageId() {
        return messageId;
    }

    /**
     * Sets the unique identifier of this message.
     *
     * @param messageId the message ID to set.
     */
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    /**
     * Gets the user ID of the sender of this message.
     *
     * @return the user ID.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the user ID of the sender of this message.
     *
     * @param userId the user ID to set.
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Gets the content of this message.
     *
     * @return the message content.
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets the content of this message.
     *
     * @param content the message content to set.
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Gets the timestamp when this message was sent.
     *
     * @return the timestamp of the message.
     */
    @DynamoDbSortKey
    public Instant getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the timestamp when this message was sent.
     *
     * @param timestamp the timestamp to set.
     */
    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
