package org.achesnokov.demo.chat.service;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.achesnokov.demo.chat.model.Message;
import org.achesnokov.demo.chat.repository.MessageRepository;

/**
 * Service class for managing message operations.
 */
@ApplicationScoped
public class MessageService {

    private final MessageRepository messageRepository;

    /**
     * Constructor for MessageService.
     *
     * @param messageRepository the message repository
     */
    @Inject
    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    /**
     * Creates a new message.
     *
     * @param chatId the ID of the chat
     * @param userId the ID of the user creating the message
     * @param content the content of the message
     * @return the created Message object
     */
    public Message createMessage(String chatId, String userId, String content) {
        Objects.requireNonNull(chatId, "Chat ID must not be null");
        Objects.requireNonNull(userId, "User ID must not be null");
        Objects.requireNonNull(content, "Content must not be null");

        Message message = new Message();
        message.setMessageId(java.util.UUID.randomUUID().toString());
        message.setChatId(chatId);
        message.setUserId(userId);
        message.setContent(content);
        message.setTimestamp(Instant.now());
        messageRepository.save(message);
        return message;
    }

    /**
     * Retrieves all messages by chat ID.
     *
     * @param chatId the ID of the chat
     * @return a list of Message objects sorted by timestamp
     */
    public List<Message> getAllMessagesByChatId(String chatId) {
        return messageRepository.findByChatIdSortedByTimestamp(chatId);
    }

}