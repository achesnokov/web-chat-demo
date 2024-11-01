package org.achesnokov.demo.chat;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * The main application class for the chat service.
 * This class extends the `jakarta.ws.rs.core.Application` class and sets the base URI path for RESTful web services.
 */
@ApplicationPath("/api")
public class ChatApplication extends Application {
}