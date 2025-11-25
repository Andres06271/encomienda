package com.encomienda.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HelloController {

    @GetMapping("/hello")
    public Map<String, String> hello() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Bienvenido al backend de Encomienda");
        response.put("status", "OK");
        response.put("version", "1.0.0");
        return response;
    }

    @GetMapping("/status")
    public Map<String, Object> status() {
        Map<String, Object> response = new HashMap<>();
        response.put("server", "Spring Boot");
        response.put("running", true);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
}
