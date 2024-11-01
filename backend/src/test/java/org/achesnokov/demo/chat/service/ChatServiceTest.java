package org.achesnokov.demo.chat.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.achesnokov.demo.chat.model.Chat;
import org.achesnokov.demo.chat.model.ChatParticipant;
import org.achesnokov.demo.chat.model.User;
import org.achesnokov.demo.chat.repository.ChatParticipantRepository;
import org.achesnokov.demo.chat.repository.ChatRepository;
import org.achesnokov.demo.chat.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ChatServiceTest {

    @InjectMocks
    ChatService chatService;

    @Mock
    ChatRepository chatRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    ChatParticipantRepository chatParticipantRepository;

    @BeforeEach
    void setUp() {
        // Any necessary setup can go here
    }

    @Test
    void createChatCreatesNewChat() {
        String userId = "user-123";
        String caption = "New Chat";

        User user = new User();
        user.setUserId(userId);
        when(userRepository.findById(userId)).thenReturn(user);

        Chat chat = new Chat();
        chat.setCaption(caption);

        doNothing().when(chatRepository).save(any(Chat.class));

        Chat result = chatService.createChat(userId, caption);

        assertNotNull(result, "Chat should be created");
        assertEquals(caption, result.getCaption(), "Chat ID should match");
        verify(chatRepository, times(1)).save(any(Chat.class));
        verify(chatParticipantRepository, times(1)).save(any(ChatParticipant.class));

    }

    @Test
    void createChatThrowsExceptionIfUserNotFound() {
        String userId = "user-123";
        String caption = "New Chat";

        when(userRepository.findById(userId)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> chatService.createChat(userId, caption), "Should throw exception if user not found");
    }

    @Test
    void addParticipantAddsNewParticipant() {
        String chatId = "chat-123";
        String userId = "user-123";

        Chat chat = new Chat();
        chat.setChatId(chatId);

        when(chatParticipantRepository.findByChatId(chatId)).thenReturn(List.of());

        ChatParticipant chatParticipant = new ChatParticipant();
        chatParticipant.setChatId(chatId);
        chatParticipant.setUserId(userId);

        doNothing().when(chatParticipantRepository).save(any(ChatParticipant.class));

        Optional<ChatParticipant> result = chatService.addParticipant(chat, userId);

        assertTrue(result.isPresent(), "Participant should be added");
        assertEquals(userId, result.get().getUserId(), "User ID should match");
        verify(chatParticipantRepository, times(1)).save(any(ChatParticipant.class));
    }

    @Test
    void addParticipantReturnsEmptyIfUserAlreadyInChat() {
        String chatId = "chat-123";
        String userId = "user-123";

        Chat chat = new Chat();
        chat.setChatId(chatId);
        ChatParticipant existingParticipant = new ChatParticipant();
        existingParticipant.setChatId(chatId);
        existingParticipant.setUserId(userId);
        existingParticipant.setJoinedAt(Instant.now());

        when(chatParticipantRepository.findByChatId(chatId)).thenReturn(List.of(existingParticipant));

        Optional<ChatParticipant> result = chatService.addParticipant(chat, userId);

        assertFalse(result.isPresent(), "Should return empty if user already in chat");
        verify(chatParticipantRepository, never()).save(any(ChatParticipant.class));
    }

    @Test
    void removeParticipantSetsLeftAt() {
        String chatId = "chat-123";
        String userId = "user-123";

        Chat chat = new Chat();
        chat.setChatId(chatId);
        ChatParticipant participant = new ChatParticipant();
        participant.setChatId(chatId);
        participant.setUserId(userId);

        when(chatRepository.findById(chatId)).thenReturn(chat);

        when(chatParticipantRepository.findByChatId(chatId)).thenReturn(List.of(participant));

        doNothing().when(chatParticipantRepository).save(any(ChatParticipant.class));

        chatService.removeParticipant(chatId, userId);

        verify(chatParticipantRepository, times(1)).save(any(ChatParticipant.class));
        assertNotNull(participant.getLeftAt(), "LeftAt should be set when participant is removed");
    }
}
