package org.achesnokov.demo.chat.service;

import java.util.List;

import org.achesnokov.demo.chat.model.Message;
import org.achesnokov.demo.chat.repository.MessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
public class MessageServiceTest {

    @InjectMocks
    MessageService messageService;

    @Mock
    MessageRepository messageRepository;

    @BeforeEach
    void setUp() {
        // Setup if necessary
    }

    @Test
    void createMessageCreatesNewMessage() {
        String chatId = "chat-123";
        String userId = "user-123";
        String content = "Hello, this is a test message!";

        doNothing().when(messageRepository).save(any(Message.class));

        Message result = messageService.createMessage(chatId, userId, content);

        assertNotNull(result, "Message should be created");
        assertEquals(chatId, result.getChatId(), "Chat ID should match");
        assertEquals(userId, result.getUserId(), "User ID should match");
        assertEquals(content, result.getContent(), "Content should match");
        assertNotNull(result.getMessageId(), "Message ID should be generated");
        assertNotNull(result.getTimestamp(), "Timestamp should be generated");
        verify(messageRepository, times(1)).save(any(Message.class));
    }

    @Test
    void createMessageGeneratesUniqueId() {
        String chatId = "chat-123";
        String userId = "user-123";
        String content = "Test message content";

        doNothing().when(messageRepository).save(any(Message.class));

        Message firstMessage = messageService.createMessage(chatId, userId, content);
        Message secondMessage = messageService.createMessage(chatId, userId, content);

        assertNotEquals(firstMessage.getMessageId(), secondMessage.getMessageId(), "Message IDs should be unique");
    }

    @Test
    void getAllMessagesByChatIdReturnsMessages() {
        String chatId = "chat-123";
        List<Message> messages = List.of(new Message(), new Message());

        when(messageRepository.findByChatIdSortedByTimestamp(chatId)).thenReturn(messages);

        List<Message> result = messageService.getAllMessagesByChatId(chatId);

        assertNotNull(result, "Messages should be returned");
        assertEquals(2, result.size(), "Should return correct number of messages");
        verify(messageRepository, times(1)).findByChatIdSortedByTimestamp(chatId);
    }

    @Test
    void getAllMessagesByChatIdReturnsEmptyListIfNoMessages() {
        String chatId = "chat-123";

        when(messageRepository.findByChatIdSortedByTimestamp(chatId)).thenReturn(List.of());

        List<Message> result = messageService.getAllMessagesByChatId(chatId);

        assertNotNull(result, "Result should not be null");
        assertTrue(result.isEmpty(), "Result should be an empty list");
        verify(messageRepository, times(1)).findByChatIdSortedByTimestamp(chatId);
    }

    @Test
    void createMessageThrowsExceptionIfContentIsNull() {
        String chatId = "chat-123";
        String userId = "user-123";
        String content = null;

        assertThrows(NullPointerException.class, () -> messageService.createMessage(chatId, userId, content), "Should throw exception if content is null");
    }

    @Test
    void createMessageThrowsExceptionIfChatIdIsNull() {
        String chatId = null;
        String userId = "user-123";
        String content = "Test message content";

        assertThrows(NullPointerException.class, () -> messageService.createMessage(chatId, userId, content), "Should throw exception if chatId is null");
    }

    @Test
    void createMessageThrowsExceptionIfUserIdIsNull() {
        String chatId = "chat-123";
        String userId = null;
        String content = "Test message content";

        assertThrows(NullPointerException.class, () -> messageService.createMessage(chatId, userId, content), "Should throw exception if userId is null");
    }
}
