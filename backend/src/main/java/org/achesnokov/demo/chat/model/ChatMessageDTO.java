package org.achesnokov.demo.chat.model;

import java.time.Instant;

/**
 * Represents a Data Transfer Object (DTO) for a chat message, containing the sender, content, and timestamp.
 */
public class ChatMessageDTO {

    private String sender;
    private String content;
    private Instant timestamp;

    /**
     * Default constructor for ChatMessageDTO.
     */
    public ChatMessageDTO() {
    }

    /**
     * Constructs a ChatMessageDTO with the given sender, content, and timestamp.
     *
     * @param sender    the sender of the message.
     * @param content   the content of the message.
     * @param timestamp the time when the message was sent.
     */
    public ChatMessageDTO(String sender, String content, Instant timestamp) {
        this.sender = sender;
        this.content = content;
        this.timestamp = timestamp;
    }

    /**
     * Gets the sender of the message.
     *
     * @return the sender.
     */
    public String getSender() {
        return sender;
    }

    /**
     * Sets the sender of the message.
     *
     * @param sender the sender to set.
     */
    public void setSender(String sender) {
        this.sender = sender;
    }

    /**
     * Gets the content of the message.
     *
     * @return the message content.
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets the content of the message.
     *
     * @param content the message content to set.
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Gets the timestamp when the message was sent.
     *
     * @return the timestamp of the message.
     */
    public Instant getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the timestamp when the message was sent.
     *
     * @param timestamp the timestamp to set.
     */
    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
