package com.example.Proyecto_IoT.service.user;

import com.example.Proyecto_IoT.dto.user.UserDTO;
import com.example.Proyecto_IoT.dto.user.AuthResponseDTO;
import com.example.Proyecto_IoT.model.User;
import com.example.Proyecto_IoT.repository.UserRepository;
import com.example.Proyecto_IoT.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthResponseDTO register(UserDTO userDTO) {
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }
        User user = new User(userDTO.getEmail(), passwordEncoder.encode(userDTO.getPassword()));
        userRepository.save(user);
        return AuthResponseDTO.builder()
                .token(jwtService.getToken(user))
                .build();
    }

    public AuthResponseDTO login(UserDTO userDTO) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userDTO.getEmail(), userDTO.getPassword()));
        User user = userRepository.findByEmail(userDTO.getEmail()).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        if (!passwordEncoder.matches(userDTO.getPassword(), user.getPassword())) {
            throw new RuntimeException("Usuario o contraseña incorrectos");
        }
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("id", user.getId());
        jwtService.setExtraClaims(extraClaims);
        String token = jwtService.getToken(user);
        return AuthResponseDTO.builder()
                .token(token)
                .build();
    }
}
