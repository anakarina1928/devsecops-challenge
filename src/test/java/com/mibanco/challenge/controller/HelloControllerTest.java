package com.mibanco.challenge.controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class HelloControllerTest {

    private final HelloController controller = new HelloController();

    @Test
    void shouldReturnGreetingWithSecretPlaceholder() {
        // En el entorno de test no existe la variable de entorno GREETING_SECRET,
        // por lo que el controlador debe usar el valor por defecto "no-configurado".
        String response = controller.hello();

        assertTrue(response.startsWith("Hola, el secreto alojado es: "));
    }

    @Test
    void shouldFallBackWhenSecretIsMissing() {
        String response = controller.hello();

        assertTrue(response.contains("no-configurado"));
    }
}
