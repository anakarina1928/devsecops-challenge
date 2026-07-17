package com.mibanco.challenge.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/hello")
    public String hello() {
        String secreto = System.getenv("GREETING_SECRET");
        if (secreto == null || secreto.isBlank()) {
            secreto = "no-configurado";
        }
        return "Hola, el secreto alojado es: " + secreto;
    }
}
