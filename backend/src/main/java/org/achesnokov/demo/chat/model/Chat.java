package org.achesnokov.demo.chat.model;

import java.time.Instant;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

/**
 * Represents a chat entity in the system.
 */
@DynamoDbBean
public class Chat {

    private String chatId;
    private String caption;
    private Instant createdAt;
    private Instant updatedAt;

    /**
     * Gets the unique identifier for the chat.
     *
     * @return the chat ID.
     */
    @DynamoDbPartitionKey
    public String getChatId() {
        return chatId;
    }

    /**
     * Sets the unique identifier for the chat.
     *
     * @param chatId the chat ID to set.
     */
    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    /**
     * Gets the caption of the chat.
     *
     * @return the chat caption.
     */
    public String getCaption() {
        return caption;
    }

    /**
     * Sets the caption of the chat.
     *
     * @param caption the chat caption to set.
     */
    public void setCaption(String caption) {
        this.caption = caption;
    }

    /**
     * Gets the timestamp when the chat was created.
     *
     * @return the creation timestamp.
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the timestamp when the chat was created.
     *
     * @param createdAt the creation timestamp to set.
     */
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Gets the timestamp when the chat was last updated.
     *
     * @return the update timestamp.
     */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Sets the timestamp when the chat was last updated.
     *
     * @param updatedAt the update timestamp to set.
     */
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
