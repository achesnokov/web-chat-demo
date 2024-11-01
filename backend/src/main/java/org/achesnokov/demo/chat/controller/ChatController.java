package org.achesnokov.demo.chat.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.achesnokov.demo.chat.model.Chat;
import org.achesnokov.demo.chat.model.ChatMessageDTO;
import org.achesnokov.demo.chat.model.ChatParticipant;
import org.achesnokov.demo.chat.model.Message;
import org.achesnokov.demo.chat.model.User;
import org.achesnokov.demo.chat.service.AuthService;
import org.achesnokov.demo.chat.service.ChatService;
import org.achesnokov.demo.chat.service.MessageService;
import org.jboss.logging.Logger;

/**
 * RESTful API controller that handles chat-related operations.
 * Provides endpoints for creating chats, managing participants, and sending/receiving messages.
 */
@Path("/chats")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class ChatController {
    private static final Logger LOGGER = Logger.getLogger(ChatController.class);

    private final ChatService chatService;
    private final MessageService messageService;
    private final AuthService authService;

    @Inject
    public ChatController(ChatService chatService, MessageService messageService, AuthService authService) {
        this.chatService = chatService;
        this.messageService = messageService;
        this.authService = authService;
    }

    /**
     * Creates a new chat.
     *
     * @param chat the chat object containing the chat caption.
     * @return Response containing the created chat object or an error message.
     */
    @POST
    public Response createChat(Chat chat) {
        if (chat == null || chat.getCaption() == null || chat.getCaption().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Caption cannot be empty").build();
        }

        try {
            return authService
                    .getCurrentUser()
                    .map(u -> chatService.createChat(u.getUserId(), chat.getCaption()))
                    .map(c -> Response.status(Response.Status.CREATED).entity(c).build())
                    .orElseGet(() -> Response.status(Response.Status.UNAUTHORIZED).entity("User not found").build());

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    /**
     * Retrieves a chat by its ID.
     *
     * @param chatId the ID of the chat to retrieve.
     * @return Response containing the chat object or an error message if not found.
     */
    @GET
    @Path("/{chatId}")
    public Response getChatById(@PathParam("chatId") String chatId) {
        Optional<Chat> chat = chatService.getChatById(chatId);
        if (chat.isPresent()) {
            return Response.ok(chat.get()).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity("Chat not found: " + chatId).build();
        }
    }

    /**
     * Retrieves all chats associated with the current user.
     *
     * @return Response containing a list of chats or an error message.
     */
    @GET
    public Response getAllUserChats() {
        return authService
                .getCurrentUser()
                .map(User::getUserId)
                .map(chatService::getChatsByUser)
                .map(chats -> Response.ok(chats).build())
                .orElseGet(() -> Response.status(Response.Status.UNAUTHORIZED).entity("User not found").build());
    }

    /**
     * Deletes a chat by its ID.
     *
     * @param chatId the ID of the chat to delete.
     * @return Response indicating the result of the delete operation.
     */
    @DELETE
    @Path("/{chatId}")
    public Response deleteChat(@PathParam("chatId") String chatId) {
        chatService.deleteChat(chatId);
        return Response.noContent().build();
    }

    /**
     * Adds the current user as a participant to a chat.
     *
     * @param chatId the ID of the chat.
     * @return Response containing the updated chat or an error message.
     */
    @POST
    @Path("/{chatId}/participants")
    public Response addParticipant(@PathParam("chatId") String chatId) {
        Optional<User> chatParticipant = authService.getCurrentUser();
        if(chatParticipant.isEmpty()){
            return Response.status(Response.Status.UNAUTHORIZED).entity("User not found").build();
        }

        Optional<Chat> chat = chatService.getChatById(chatId);

        if(chat.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).entity("Chat not found: " + chatId).build();
        }

        return chatService.addParticipant(chat.get(), chatParticipant.get().getUserId())
                .map(c -> Response.status(Response.Status.OK).entity(c).build())
                .orElseGet(() -> Response.status(Response.Status.NO_CONTENT).build());
    }

    /**
     * Removes the current user from a chat.
     *
     * @param chatId the ID of the chat.
     * @return Response indicating the result of the removal operation.
     */
    @DELETE
    @Path("/{chatId}/participants")
    public Response removeParticipant(@PathParam("chatId") String chatId) {
        return authService
                .getCurrentUser()
                .map(User::getUserId)
                .map(id -> {
                    chatService.removeParticipant(chatId, id);
                    return Response.status(Response.Status.OK).entity("User removed from chat").build();
                })
                .orElseGet(() ->
                        Response.status(Response.Status.UNAUTHORIZED).entity("User not found").build());
    }

    /**
     * Retrieves the current participants of a chat.
     *
     * @param chatId the ID of the chat.
     * @return Response containing a list of chat participants.
     */
    @GET
    @Path("/{chatId}/participants")
    public Response getCurrentParticipants(@PathParam("chatId") String chatId) {
        List<ChatParticipant> participants = chatService.getCurrentChatParticipants(chatId);
        return Response.ok(participants).build();
    }

    /**
     * Retrieves all messages in a chat.
     *
     * @param chatId the ID of the chat.
     * @return Response containing a list of messages or an error message.
     */
    @GET
    @Path("/{chatId}/messages")
    public Response getMessagesByChatId(@PathParam("chatId") String chatId) {
        Map<String, String> chatUsers = chatService
                .getChartParticipantsUsers(chatId)
                .stream()
                .collect(Collectors.toMap(User::getUserId, User::getUsername));

        try {
            List<ChatMessageDTO> messages = messageService
                    .getAllMessagesByChatId(chatId)
                    .stream()
                    .map(m -> toChatMessageDTO(m, chatUsers.get(m.getUserId())))
                    .toList();

            return Response.ok(messages).build();
        } catch (Exception e) {
            LOGGER.error("An error occurred while fetching messages for chatId: " + chatId, e);
            return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred while fetching messages: " + e.getMessage()).build();
        }
    }

    /**
     * Sends a message in a chat.
     *
     * @param chatId the ID of the chat.
     * @param message the message object containing the content to be sent.
     * @return Response containing the created message or an error message.
     */
    @POST
    @Path("/{chatId}/messages")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response sendMessage(@PathParam("chatId") String chatId, Message message) {
        if (message == null || message.getContent() == null || message.getContent().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Message content cannot be empty").build();
        }

        return authService
                .getCurrentUser()
                .map(u -> {
                    Message m = messageService.createMessage(chatId, u.getUserId(), message.getContent());
                    return Response
                            .status(Response.Status.CREATED)
                            .entity(toChatMessageDTO(m, u.getUsername()))
                            .build();
                })
                .orElseGet(() -> Response.status(Response.Status.UNAUTHORIZED).entity("User not found").build());

    }

    /**
     * Converts a Message object to a ChatMessageDTO.
     *
     * @param message the message object to convert.
     * @param userName the username of the message sender.
     * @return the converted ChatMessageDTO.
     */
    private ChatMessageDTO toChatMessageDTO(Message message, String userName) {
        return new ChatMessageDTO(userName, message.getContent(), message.getTimestamp());
    }
}
