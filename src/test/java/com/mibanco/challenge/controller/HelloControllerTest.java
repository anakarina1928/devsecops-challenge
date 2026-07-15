package com.mibanco.challenge.controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HelloControllerTest {

    private final HelloController controller = new HelloController();

    @Test
    void shouldReturnGreetingMessage() {
        String response = controller.hello();

        assertEquals("Hola desde el microservicio DevSecOps Challenge - Mibanco", response);
    }

    @Test
    void shouldMentionMibanco() {
        String response = controller.hello();

        assertTrue(response.contains("Mibanco"));
    }
}
