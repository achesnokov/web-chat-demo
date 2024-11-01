package org.achesnokov.demo.chat.controller;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * RESTful API controller that provides configuration information for the web-chat-demo application.
 * Offers an endpoint to retrieve the WebSocket host configuration.
 */
@Path("/config")
public class ConfigController {

    @ConfigProperty(name = "org.achesnokov.demo.chat.websocket_host")
    String websocketUrl;

    /**
     * Retrieves the WebSocket host configuration.
     *
     * @return Response containing the WebSocket host configuration in JSON format.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getConfig() {
        String jsonResponse = String.format("{\"websocketHost\": \"%s\"}", websocketUrl);
        return Response.ok(jsonResponse).build();
    }
}
