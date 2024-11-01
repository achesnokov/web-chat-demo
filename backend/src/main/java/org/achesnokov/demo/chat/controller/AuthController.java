package org.achesnokov.demo.chat.controller;

import io.quarkus.security.Authenticated;
import jakarta.annotation.security.PermitAll;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.achesnokov.demo.chat.model.AuthRequest;
import org.achesnokov.demo.chat.model.AuthResponse;
import org.achesnokov.demo.chat.service.AuthService;
import org.jboss.logging.Logger;

/**
 * RESTful API controller that handles user authentication for the web-chat-demo application.
 * Provides endpoints for user registration, login, and token validation.
 */
@RequestScoped
@Path("/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthController {


    private static final Logger LOGGER = Logger.getLogger(AuthController.class);

    @Inject
    AuthService authService;

    /**
     * Registers a new user.
     *
     * @param authRequest the authentication request containing username and password.
     * @return Response containing JWT token if registration is successful, or an error message otherwise.
     */
    @POST
    @Path("/register")
    @PermitAll
    public Response register(AuthRequest authRequest) {
        try {
            String token = authService.register(authRequest.getUsername(), authRequest.getPassword());
            return Response.ok(new AuthResponse(token)).build();
        } catch (IllegalArgumentException e) {
            LOGGER.debug("Failed to register user: " + authRequest.getUsername());
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    /**
     * Logs in an existing user.
     *
     * @param authRequest the authentication request containing username and password.
     * @return Response containing JWT token if authentication is successful, or an error message otherwise.
     */
    @POST
    @Path("/login")
    @PermitAll
    public Response login(AuthRequest authRequest) {
        try {
            String token = authService.login(authRequest.getUsername(), authRequest.getPassword());
            return Response.ok(new AuthResponse(token)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
        }
    }

    /**
     * Validates a JWT token.
     *
     * @return Response indicating if the token is valid.
     */
    @GET
    @Path("/validate")
    @Authenticated
    public Response validateToken() {
        return Response.ok().entity("Token is valid").build();
    }
}
