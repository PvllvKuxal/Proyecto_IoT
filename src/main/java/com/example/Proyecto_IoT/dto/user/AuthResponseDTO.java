package com.example.Proyecto_IoT.dto.user;

import com.example.Proyecto_IoT.dto.device.DeviceDTO;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDTO {
    private String token;
} 