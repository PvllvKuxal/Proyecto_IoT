package com.example.Proyecto_IoT.controller;

import com.example.Proyecto_IoT.dto.user.UserDTO;
import com.example.Proyecto_IoT.dto.user.AuthResponseDTO;
import com.example.Proyecto_IoT.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody UserDTO userDTO) {
        userService.register(userDTO);
        return ResponseEntity.ok("Usuario registrado correctamente");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody UserDTO userDTO) {
        AuthResponseDTO response = userService.login(userDTO);
        return ResponseEntity.ok(response);
    }
} 