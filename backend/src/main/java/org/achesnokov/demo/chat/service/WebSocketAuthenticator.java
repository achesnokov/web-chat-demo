package org.achesnokov.demo.chat.service;

import java.util.List;

import io.quarkus.arc.Arc;
import io.quarkus.arc.ArcContainer;
import io.quarkus.arc.InstanceHandle;
import io.smallrye.jwt.auth.principal.JWTParser;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;

/**
 * WebSocketAuthenticator is responsible for authenticating WebSocket connections
 * using JWT tokens provided during the handshake process.
 */
@ApplicationScoped
public class WebSocketAuthenticator extends ServerEndpointConfig.Configurator {
    AutoCloseable autoCloseable;
    private static final Logger LOGGER = Logger.getLogger(WebSocketAuthenticator.class);

    /**
     * Modifies the WebSocket handshake to include JWT authentication.
     *
     * @param config the ServerEndpointConfig
     * @param request the HandshakeRequest
     * @param response the HandshakeResponse
     * @throws SecurityException if no token is provided or if token parsing fails
     */
    @Override
    public void modifyHandshake(ServerEndpointConfig config, HandshakeRequest request, HandshakeResponse response) {
        List<String> tokens = request.getParameterMap().get("token");
        if (tokens == null || tokens.isEmpty()) {
            LOGGER.error("No token provided in handshake");
            throw new SecurityException("No token provided");
        }

        try {
            JWTParser parser = getJWTParser();
            if (parser == null) {
                LOGGER.error("Failed to obtain JWTParser instance from Arc");
                throw new SecurityException("JWTParser instance unavailable");
            }
            JsonWebToken jwt = parser.parse(tokens.get(0));
            LOGGER.debug("Token parsed successfully, subject: " + jwt.getSubject());
            config.getUserProperties().put("jwt", jwt);
        } catch (Exception e) {
            LOGGER.error("Failed to parse token", e);
            throw new SecurityException("Invalid token");
        }
    }

    protected JWTParser getJWTParser() {
        try(InstanceHandle<JWTParser> instanceHandle = Arc.container().instance(JWTParser.class)) {
            return instanceHandle.get();
        }
    }
}