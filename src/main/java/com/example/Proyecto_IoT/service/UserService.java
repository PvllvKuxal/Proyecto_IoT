package com.example.Proyecto_IoT.service;

import com.example.Proyecto_IoT.dto.user.UserDTO;
import com.example.Proyecto_IoT.dto.user.AuthResponseDTO;
import com.example.Proyecto_IoT.dto.device.DeviceDTO;
import com.example.Proyecto_IoT.model.User;
import com.example.Proyecto_IoT.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    // Aquí se podrían inyectar servicios de devices y pdfs en el futuro

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void register(UserDTO userDTO) {
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }
        User user = new User(userDTO.getEmail(), passwordEncoder.encode(userDTO.getPassword()));
        userRepository.save(user);
    }

    public AuthResponseDTO login(UserDTO userDTO) {
        User user = userRepository.findByEmail(userDTO.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario o contraseña incorrectos"));
        if (!passwordEncoder.matches(userDTO.getPassword(), user.getPassword())) {
            throw new RuntimeException("Usuario o contraseña incorrectos");
        }
        // Aquí se debe generar el JWT (mock por ahora)
        String token = "mock-jwt-token";
        // Aquí se obtendrán los devices y pdfs (mock por ahora)
        List<DeviceDTO> devices = Collections.emptyList();
        List<String> pdfPaths = Collections.emptyList();
        return new AuthResponseDTO(token, devices, pdfPaths);
    }
} 