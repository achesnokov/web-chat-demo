package org.achesnokov.demo.chat.controller;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import io.quarkus.test.security.TestSecurity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.achesnokov.demo.chat.model.Chat;
import org.achesnokov.demo.chat.model.ChatParticipant;
import org.achesnokov.demo.chat.model.Message;
import org.achesnokov.demo.chat.model.User;
import org.achesnokov.demo.chat.service.AuthService;
import org.achesnokov.demo.chat.service.ChatService;
import org.achesnokov.demo.chat.service.MessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@QuarkusTest
public class ChatControllerTest {

    @InjectMock
    ChatService chatService;

    @InjectMock
    MessageService messageService;

    @InjectMock
    AuthService authService;

    @Test
    @TestSecurity(user = "user-123")
    void testCreateChatSuccessfully() {
        Chat chat = new Chat();
        chat.setCaption("Test Chat");

        User currentUser = new User();
        currentUser.setUserId("user-123");

        when(authService.getCurrentUser()).thenReturn(Optional.of(currentUser));
        when(chatService.createChat(anyString(), anyString())).thenReturn(chat);

        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(chat)
                .when().post("/api/chats")
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode())
                .body("caption", is("Test Chat"));
    }

    @Test
    @TestSecurity(user = "user-123")
    void testCreateChatFailsWhenCaptionIsEmpty() {
        Chat chat = new Chat();
        chat.setCaption("");

        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(chat)
                .when().post("/api/chats")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .body(is("Caption cannot be empty"));
    }

    @Test
    @TestSecurity(user = "user-123")
    void testGetChatByIdSuccessfully() {
        String chatId = "chat-123";
        Chat chat = new Chat();
        chat.setChatId(chatId);
        chat.setCaption("Test Chat");

        when(chatService.getChatById(anyString())).thenReturn(Optional.of(chat));

        given()
                .when().get("/api/chats/" + chatId)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("chatId", is(chatId))
                .body("caption", is("Test Chat"));
    }

    @Test
    @TestSecurity(user = "user-123")
    void testGetChatByIdNotFound() {
        String chatId = "chat-unknown";

        when(chatService.getChatById(anyString())).thenReturn(Optional.empty());

        given()
                .when().get("/api/chats/" + chatId)
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode())
                .body(is("Chat not found: " + chatId));
    }

    @Test
    @TestSecurity(user = "user-123")
    void testGetAllUserChatsSuccessfully() {
        User currentUser = new User();
        currentUser.setUserId("user-123");

        Chat chat1 = new Chat();
        chat1.setChatId("chat-1");
        chat1.setCaption("Chat 1");

        Chat chat2 = new Chat();
        chat2.setChatId("chat-2");
        chat2.setCaption("Chat 2");

        when(authService.getCurrentUser()).thenReturn(Optional.of(currentUser));
        when(chatService.getChatsByUser(anyString())).thenReturn(List.of(chat1, chat2));

        given()
                .when().get("/api/chats")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("size()", is(2))
                .body("chatId", containsInAnyOrder("chat-1", "chat-2"))
                .body("caption", containsInAnyOrder("Chat 1", "Chat 2"));
    }

    @Test
    @TestSecurity(user = "user-123")
    void testGetAllUserChatsUserNotFound() {
        when(authService.getCurrentUser()).thenReturn(Optional.empty());

        given()
                .when().get("/api/chats")
                .then()
                .statusCode(Response.Status.UNAUTHORIZED.getStatusCode())
                .body(is("User not found"));
    }

    @Test
    @TestSecurity(user = "user-123")
    void testDeleteChatSuccessfully() {
        String chatId = "chat-123";

        Mockito.doNothing().when(chatService).deleteChat(anyString());

        given()
                .when().delete("/api/chats/" + chatId)
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());
    }

    @Test
    @TestSecurity(user = "user-123")
    void testDeleteChatException() {
        String chatId = "chat-unknown";

        Mockito.doThrow(new IllegalArgumentException("Chat not found")).when(chatService).deleteChat(anyString());

        given()
                .when().delete("/api/chats/" + chatId)
                .then()
                .statusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @Test
    @TestSecurity(user = "user-123")
    void testAddParticipantSuccessfully() {
        String chatId = "chat-123";
        Chat chat = new Chat();
        chat.setChatId(chatId);

        User currentUser = new User();
        currentUser.setUserId("user-123");

        ChatParticipant chatParticipant = new ChatParticipant();
        chatParticipant.setChatId(chatId);
        chatParticipant.setUserId("user-123");

        when(chatService.getChatById(anyString())).thenReturn(Optional.of(chat));
        when(authService.getCurrentUser()).thenReturn(Optional.of(currentUser));
        when(chatService.addParticipant(any(Chat.class), anyString())).thenReturn(Optional.of(chatParticipant));

        given()
                .contentType(MediaType.APPLICATION_JSON)
                .when().post("/api/chats/" + chatId + "/participants")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("chatId", is(chatId));
    }

    @Test
    @TestSecurity(user = "user-123")
    void testAddParticipantChatNotFound() {
        String chatId = "chat-unknown";
        User currentUser = new User();
        currentUser.setUserId("user-123");

        when(authService.getCurrentUser()).thenReturn(Optional.of(currentUser));
        when(chatService.getChatById(anyString())).thenReturn(Optional.empty());

        given()
                .contentType(MediaType.APPLICATION_JSON)
                .when().post("/api/chats/" + chatId + "/participants")
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode())
                .body(is("Chat not found: " + chatId));
    }

    @Test
    @TestSecurity(user = "user-123")
    void testRemoveParticipantSuccessfully() {
        String chatId = "chat-123";
        User currentUser = new User();
        currentUser.setUserId("user-123");

        when(authService.getCurrentUser()).thenReturn(Optional.of(currentUser));

        Mockito.doNothing().when(chatService).removeParticipant(anyString(), anyString());

        given()
                .when().delete("/api/chats/" + chatId + "/participants")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body(is("User removed from chat"));
    }

    @Test
    @TestSecurity(user = "user-123")
    void testRemoveParticipantUserNotFound() {
        String chatId = "chat-123";

        when(authService.getCurrentUser()).thenReturn(Optional.empty());

        given()
                .when().delete("/api/chats/" + chatId + "/participants")
                .then()
                .statusCode(Response.Status.UNAUTHORIZED.getStatusCode())
                .body(is("User not found"));
    }
}
