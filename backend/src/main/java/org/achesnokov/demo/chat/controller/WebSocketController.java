package org.achesnokov.demo.chat.controller;

import java.io.IOException;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import io.vertx.core.Vertx;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.websocket.CloseReason;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import org.achesnokov.demo.chat.model.ChatParticipant;
import org.achesnokov.demo.chat.model.Message;
import org.achesnokov.demo.chat.model.User;
import org.achesnokov.demo.chat.repository.UserRepository;
import org.achesnokov.demo.chat.service.ChatService;
import org.achesnokov.demo.chat.service.MessageService;
import org.achesnokov.demo.chat.service.WebSocketAuthenticator;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;

@ServerEndpoint(
        value = "/chat/ws/{chatId}",
        configurator = WebSocketAuthenticator.class
)
@ApplicationScoped
public class WebSocketController {
    private static final Logger LOGGER = Logger.getLogger(WebSocketController.class);
    private static final Map<String, Set<Session>> chatSessions = new ConcurrentHashMap<>();

    private enum MessageType {
        SYSTEM, MESSAGE, ERROR
    }

    @Inject
    ChatService chatService;

    @Inject
    UserRepository userRepository;

    @Inject
    MessageService messageService;

    /**
     * Handles a new WebSocket connection.
     *
     * @param session the WebSocket session being opened.
     */
    @OnOpen
    public void onOpen(Session session) {
        String chatId = getChatIdFromSession(session);
        Vertx.vertx().runOnContext(ignored -> handleOpenSession(session, chatId));
    }

    private void handleOpenSession(Session session, String chatId) {
        try {
            User user = authenticateUser(session);
            validateChatParticipant(user, chatId);
            initializeSession(session, user, chatId);
            sendWelcomeMessages(session, chatId);
        } catch (Exception e) {
            handleSessionError(session, e);
        }
    }

    private User authenticateUser(Session session) throws Exception {
        JsonWebToken jwt = (JsonWebToken) session.getUserProperties().get("jwt");
        return userRepository.findByUsername(jwt.getSubject())
                .orElseThrow(() -> new IllegalStateException("User not found"));
    }

    private void validateChatParticipant(User user, String chatId) {
        boolean isParticipant = chatService.getChatParticipants(chatId).stream()
                .map(ChatParticipant::getUserId)
                .anyMatch(user.getUserId()::equals);

        if (!isParticipant) {
            throw new IllegalStateException("User is not a participant of this chat");
        }
    }

    private void initializeSession(Session session, User user, String chatId) {
        session.getUserProperties().put("userId", user.getUserId());
        session.getUserProperties().put("username", user.getUsername());
        chatSessions.computeIfAbsent(chatId, k -> ConcurrentHashMap.newKeySet()).add(session);
        LOGGER.info(String.format("Session opened: %s for chat: %s by user: %s", session.getId(), chatId, user.getUsername()));
    }

    private void sendWelcomeMessages(Session session, String chatId) {
        sendMessage(session, createMessage(MessageType.SYSTEM, "Connected to chat"));
        sendChatHistory(session, chatId);
    }

    private void sendChatHistory(Session session, String chatId) {
        Map<String, String> chatUsers = chatService.getChartParticipantsUsers(chatId).stream()
                .collect(Collectors.toMap(User::getUserId, User::getUsername));

        messageService.getAllMessagesByChatId(chatId).forEach(msg -> {
            JsonObject messageObj = Json.createObjectBuilder()
                    .add("type", MessageType.MESSAGE.name().toLowerCase())
                    .add("username", chatUsers.get(msg.getUserId()))
                    .add("content", msg.getContent())
                    .add("timestamp", msg.getTimestamp().toString())
                    .build();
            sendMessage(session, messageObj);
        });
    }

    /**
     * Handles incoming WebSocket messages.
     *
     * @param message the message sent by the client.
     * @param session the session from which the message was sent.
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        Vertx.vertx().runOnContext(v -> handleMessage(message, session));
    }

    private void handleMessage(String message, Session session) {
        String chatId = getChatIdFromSession(session);
        String userId = getUserIdFromSession(session);
        String username = getUsernameFromSession(session);

        try {
            Message newMessage = messageService.createMessage(chatId, userId, message);
            JsonObject messageObj = Json.createObjectBuilder()
                    .add("type", MessageType.MESSAGE.name().toLowerCase())
                    .add("username", username)
                    .add("content", message)
                    .add("timestamp", newMessage.getTimestamp().toString())
                    .build();

            distributeMessageToParticipants(chatId, null, messageObj);
        } catch (Exception e) {
            LOGGER.error("Error processing message", e);
            sendMessage(session, createMessage(MessageType.ERROR, "Failed to process message"));
        }
    }

    /**
     * Handles WebSocket disconnection events.
     *
     * @param session the WebSocket session being closed.
     */
    @OnClose
    public void onClose(Session session) {
        Vertx.vertx().runOnContext(v -> {
            String chatId = getChatIdFromSession(session);
            handleSessionClose(session, chatId);
        });
    }

    private void handleSessionClose(Session session, String chatId) {
        Set<Session> sessions = chatSessions.get(chatId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                chatSessions.remove(chatId);
            }

            String username = getUsernameFromSession(session);
            distributeMessageToParticipants(chatId, session,
                    createMessage(MessageType.SYSTEM, "User " + username + " disconnected from chat"));
        }
        LOGGER.info(String.format("Session closed: %s for chat: %s", session.getId(), chatId));
    }

    /**
     * Handles errors that occur during WebSocket communication.
     *
     * @param session the WebSocket session where the error occurred.
     * @param throwable the exception thrown during communication.
     */
    @OnError
    public void onError(Session session, Throwable throwable) {
        Vertx.vertx().runOnContext(v -> {
            LOGGER.error("WebSocket error for session: {}", session.getId(), throwable);
            sendMessage(session, createMessage(MessageType.ERROR, "WebSocket error occurred"));
        });
    }

    private void sendMessage(Session session, JsonObject message) {
        session.getAsyncRemote().sendText(message.toString(), result -> {
            if (!result.isOK()) {
                LOGGER.error("Error sending message to client: {}", session.getId(), result.getException());
            }
        });
    }

    private JsonObject createMessage(MessageType type, String content) {
        return Json.createObjectBuilder()
                .add("type", type.name().toLowerCase())
                .add("content", content)
                .add("timestamp", Instant.now().toString())
                .build();
    }

    private void distributeMessageToParticipants(String chatId, Session excludeSession, JsonObject message) {
        Optional
                .ofNullable(chatSessions.get(chatId))
                .stream()
                .flatMap(Collection::stream)
                .filter(session -> !session.equals(excludeSession))
                .forEach(session -> sendMessage(session, message));
    }

    private String getChatIdFromSession(Session session) {
        return session.getPathParameters().get("chatId");
    }

    private String getUserIdFromSession(Session session) {
        return (String) session.getUserProperties().get("userId");
    }

    private String getUsernameFromSession(Session session) {
        return (String) session.getUserProperties().get("username");
    }

    private void handleSessionError(Session session, Exception e) {
        LOGGER.error("Failed to authenticate WebSocket connection", e);
        try {
            session.close(new CloseReason(
                    CloseReason.CloseCodes.VIOLATED_POLICY,
                    "Authentication failed: " + e.getMessage()
            ));
        } catch (IOException ex) {
            LOGGER.error("Error closing unauthorized session", ex);
        }
    }
}
