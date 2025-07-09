package com.example.Proyecto_IoT.controller;

import com.example.Proyecto_IoT.dto.user.UserDTO;
import com.example.Proyecto_IoT.dto.user.AuthResponseDTO;
import com.example.Proyecto_IoT.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody UserDTO userDTO) {
        authService.register(userDTO);
        return ResponseEntity.ok("Usuario registrado correctamente");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody UserDTO userDTO) {
        AuthResponseDTO response = authService.login(userDTO);
        return ResponseEntity.ok(response);
    }
} 