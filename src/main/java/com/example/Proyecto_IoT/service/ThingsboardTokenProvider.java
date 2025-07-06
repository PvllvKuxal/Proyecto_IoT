package com.example.Proyecto_IoT.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class ThingsboardTokenProvider implements ApplicationRunner {

    @Value("${thingsboard.api.url}")
    private String tbApiUrl;

    @Value("${thingsboard.username}")
    private String username;

    @Value("${thingsboard.password}")
    private String password;

    private String jwtToken;
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public void run(ApplicationArguments args) {
        String loginUrl = tbApiUrl + "/api/auth/login";
        Map<String, String> loginBody = new HashMap<>();
        loginBody.put("username", username);
        loginBody.put("password", password);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(loginBody, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(loginUrl, request, Map.class);
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            this.jwtToken = (String) response.getBody().get("token");
            System.out.println("Token JWT obtenido correctamente al iniciar la app");
        } else {
            throw new RuntimeException("No se pudo obtener el token JWT de ThingsBoard");
        }
    }

    public String getJwtToken() {
        return jwtToken;
    }
}