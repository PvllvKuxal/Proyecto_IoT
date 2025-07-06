package com.example.Proyecto_IoT.service;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.Map;

@Service
public class ThingsBoardAuthService {

    @Value("${thingsboard.api.url}")
    private String tbApiUrl;
    @Value("${thingsboard.username}")
    private String username;
    @Value("${thingsboard.password}")
    private String password;

    private String jwtToken; // Guarda el token en memoria (mejor usar cache o db para producción)
    private RestTemplate restTemplate = new RestTemplate();

    public String login(String username, String password) {
        String url = tbApiUrl + "/api/auth/login";
        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", username);
        credentials.put("password", password);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(credentials, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
        jwtToken = (String) response.getBody().get("token");
        return jwtToken;
    }

    public String getJwtToken() {
        if (jwtToken == null) {
            login(username, password);
        }
        return jwtToken;
    }
}

