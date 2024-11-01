package org.achesnokov.demo.chat.service;

import java.util.Collections;
import java.util.List;

import io.smallrye.jwt.auth.principal.JWTParser;
import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class WebSocketAuthenticatorTest {

    @Spy
    @InjectMocks
    WebSocketAuthenticator webSocketAuthenticator;

    @Mock
    JWTParser jwtParser;

    @Mock
    HandshakeRequest handshakeRequest;

    @Mock
    HandshakeResponse handshakeResponse;

    @Mock
    ServerEndpointConfig serverEndpointConfig;

    @BeforeEach
    void setUp() {
        // No need for Arc.container() anymore, we can mock getJWTParser() with lenient stubbing
        lenient().doReturn(jwtParser).when(webSocketAuthenticator).getJWTParser();
    }

    @Test
    void modifyHandshakeThrowsExceptionIfTokenIsMissing() {
        when(handshakeRequest.getParameterMap()).thenReturn(Collections.emptyMap());

        assertThrows(SecurityException.class, () ->
                        webSocketAuthenticator.modifyHandshake(serverEndpointConfig, handshakeRequest, handshakeResponse),
                "Should throw SecurityException if token is missing");
    }

    @Test
    void modifyHandshakeThrowsExceptionIfTokenIsInvalid() throws Exception {
        List<String> tokens = List.of("invalid-token");
        when(handshakeRequest.getParameterMap()).thenReturn(Collections.singletonMap("token", tokens));
        when(jwtParser.parse(anyString())).thenThrow(new RuntimeException("Invalid token"));

        assertThrows(SecurityException.class, () ->
                        webSocketAuthenticator.modifyHandshake(serverEndpointConfig, handshakeRequest, handshakeResponse),
                "Should throw SecurityException if token is invalid");
    }

    @Test
    void modifyHandshakeThrowsExceptionIfJwtParserIsNull() {
        // Simulate JWTParser being unavailable
        doReturn(null).when(webSocketAuthenticator).getJWTParser();

        List<String> tokens = List.of("valid-token");
        when(handshakeRequest.getParameterMap()).thenReturn(Collections.singletonMap("token", tokens));

        assertThrows(SecurityException.class, () ->
                        webSocketAuthenticator.modifyHandshake(serverEndpointConfig, handshakeRequest, handshakeResponse),
                "Should throw SecurityException if JWTParser is unavailable");
    }

    @Test
    void modifyHandshakeParsesTokenSuccessfully() throws Exception {
        List<String> tokens = List.of("valid-token");
        JsonWebToken jwt = mock(JsonWebToken.class);
        when(handshakeRequest.getParameterMap()).thenReturn(Collections.singletonMap("token", tokens));
        when(jwtParser.parse(tokens.get(0))).thenReturn(jwt);

        webSocketAuthenticator.modifyHandshake(serverEndpointConfig, handshakeRequest, handshakeResponse);

        verify(serverEndpointConfig, times(1)).getUserProperties();
        verify(jwt, times(1)).getSubject();
    }
}
