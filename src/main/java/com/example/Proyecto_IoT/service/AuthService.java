package com.example.Proyecto_IoT.service;

import com.example.Proyecto_IoT.dto.user.UserDTO;
import com.example.Proyecto_IoT.dto.user.AuthResponseDTO;
import com.example.Proyecto_IoT.dto.device.DeviceDTO;
import com.example.Proyecto_IoT.model.User;
import com.example.Proyecto_IoT.repository.UserRepository;
import com.example.Proyecto_IoT.repository.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    @Autowired
    private DeviceRepository deviceRepository;
    // Aquí se podrían inyectar servicios de devices y pdfs en el futuro

    @Autowired
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
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
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        if (!passwordEncoder.matches(userDTO.getPassword(), user.getPassword())) {
            throw new RuntimeException("Usuario o contraseña incorrectos");
        }
        // Obtener los dispositivos del usuario desde la BD
        List<DeviceDTO> devices = deviceRepository.findAll().stream()
                .filter(d -> d.getUser().getId().equals(user.getId()))
                .map(d -> new DeviceDTO(
                        d.getId(),
                        d.getThingsboardId(),
                        d.getName(),
                        d.getType(),
                        d.getUser().getId()
                ))
                .collect(Collectors.toList());
        // Aquí se debe generar el JWT (mock por ahora)
        String token = "mock-jwt-token";
        // Aquí se obtendrán los pdfs (mock por ahora)
        List<String> pdfPaths = Collections.emptyList();
        return new AuthResponseDTO(token, devices, pdfPaths);
    }
}
