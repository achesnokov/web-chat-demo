package org.achesnokov.demo.chat.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.achesnokov.demo.chat.model.Chat;
import org.achesnokov.demo.chat.model.ChatParticipant;
import org.achesnokov.demo.chat.model.User;
import org.achesnokov.demo.chat.repository.ChatParticipantRepository;
import org.achesnokov.demo.chat.repository.ChatRepository;
import org.achesnokov.demo.chat.repository.UserRepository;

/**
 * Service class for managing chat operations.
 */
@ApplicationScoped
public class ChatService {

    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final ChatParticipantRepository chatParticipantRepository;

    /**
     * Constructor for ChatService.
     *
     * @param chatRepository the chat repository
     * @param userRepository the user repository
     * @param chatParticipantRepository the chat participant repository
     */
    @Inject
    public ChatService(ChatRepository chatRepository, UserRepository userRepository, ChatParticipantRepository chatParticipantRepository) {
        this.chatRepository = chatRepository;
        this.userRepository = userRepository;
        this.chatParticipantRepository = chatParticipantRepository;
    }

    /**
     * Creates a new chat.
     *
     * @param userId the ID of the user creating the chat
     * @param caption the caption of the chat
     * @return the created Chat object
     * @throws IllegalArgumentException if the user is not found
     */
    public Chat createChat(String userId, String caption) {
        User user = userRepository.findById(userId);
        if (user == null) {
            //TODO: replace it with application specific exception
            throw new IllegalArgumentException("User not found: " + userId);
        }

        String chatId = UUID.randomUUID().toString();
        Chat chat = new Chat();
        chat.setChatId(chatId);
        chat.setCaption(caption);
        chat.setCreatedAt(Instant.now());
        chatRepository.save(chat);

        addParticipant(chat, userId);

        return chat;
    }

    /**
     * Retrieves a chat by its ID.
     *
     * @param chatId the ID of the chat
     * @return an Optional containing the Chat if found, or empty if not found
     */
    public Optional<Chat> getChatById(String chatId) {
        Chat chat = chatRepository.findById(chatId);
        return Optional.ofNullable(chat);
    }

    /**
     * Retrieves all chats.
     *
     * @return a list of all Chat objects
     */
    public List<Chat> getAllChats() {
        return chatRepository.findAll();
    }

    /**
     * Retrieves chats by user ID.
     *
     * @param userId the ID of the user
     * @return a list of Chat objects associated with the user
     */
    public List<Chat> getChatsByUser(String userId) {
        return chatRepository.findByParticipant(userId);
    }

    /**
     * Deletes a chat by its ID.
     *
     * @param chatId the ID of the chat to delete
     */
    public void deleteChat(String chatId) {
        chatRepository.delete(chatId);
    }

    /**
     * Retrieves participants of a chat by chat ID.
     *
     * @param chatId the ID of the chat
     * @return a list of ChatParticipant objects
     */
    public List<ChatParticipant> getChatParticipants(String chatId) {
        return chatParticipantRepository.findByChatId(chatId);
    }

    /**
     * Retrieves users participating in a chat by chat ID.
     *
     * @param chatId the ID of the chat
     * @return a set of User objects
     */
    public Set<User> getChartParticipantsUsers(String chatId) {
        Set<String> userIds = getChatParticipants(chatId)
                .stream()
                .map(ChatParticipant::getUserId)
                .collect(Collectors.toSet());

        return userRepository.findByIds(userIds);
    }

    /**
     * Retrieves current participants of a chat by chat ID.
     *
     * @param chatId the ID of the chat
     * @return a list of ChatParticipant objects who have not left the chat
     */
    public List<ChatParticipant> getCurrentChatParticipants(String chatId) {
        return chatParticipantRepository.findByChatId(chatId).stream()
                .filter(p -> p.getLeftAt() == null)
                .collect(Collectors.toList());
    }

    /**
     * Adds a participant to a chat.
     *
     * @param chat the Chat object
     * @param userId the ID of the user to add
     * @return an Optional containing the ChatParticipant if added, or empty if the user is already in the chat
     */
    public Optional<ChatParticipant> addParticipant(Chat chat, String userId) {
        Optional<ChatParticipant> existingChatParticipant = chatParticipantRepository
                .findByChatId(chat.getChatId())
                .stream()
                .filter(p -> p.getUserId().equals(userId))
                .findFirst();

        if(existingChatParticipant.filter(p -> p.getLeftAt() == null).isPresent()) {
            // User already in chat
            return Optional.empty();
        }

        ChatParticipant chatParticipant = existingChatParticipant
                .orElseGet(() -> {
                    ChatParticipant p = new ChatParticipant();
                    p.setChatId(chat.getChatId());
                    p.setUserId(userId);
                    p.setJoinedAt(Instant.now());
                    return p;
                });
        chatParticipant.setLeftAt(null);

        chatParticipantRepository.save(chatParticipant);
        return Optional.of(chatParticipant);
    }

    /**
     * Removes a participant from a chat.
     *
     * @param chatId the ID of the chat
     * @param userId the ID of the user to remove
     */
    public void removeParticipant(String chatId, String userId) {
        getChatById(chatId)
                .flatMap(chat -> chatParticipantRepository.findByChatId(chatId).stream()
                        .filter(p -> p.getUserId().equals(userId))
                        .findFirst())
                .ifPresent(participant -> {
                    participant.setLeftAt(Instant.now());
                    chatParticipantRepository.save(participant);
                });
    }
}
