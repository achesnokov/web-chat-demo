package org.achesnokov.demo.chat.controller;

import java.util.Map;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

@QuarkusTest
public class ConfigControllerTest {

    @Test
    void testGetConfigSuccessfully() {
        given()
                .when().get("/api/config")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body("websocketHost", is("ws://localhost:8080"));
    }

}


