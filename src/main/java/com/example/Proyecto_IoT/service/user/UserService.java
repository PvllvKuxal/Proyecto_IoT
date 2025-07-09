package com.example.Proyecto_IoT.service.user;


import com.example.Proyecto_IoT.dto.device.DeviceDTO;
import com.example.Proyecto_IoT.dto.medicion.MedicionDTO;
import com.example.Proyecto_IoT.dto.user.UserDevicesDTO;
import com.example.Proyecto_IoT.dto.user.UserMedicionesDTO;
import com.example.Proyecto_IoT.model.Device;
import com.example.Proyecto_IoT.model.Medicion;
import com.example.Proyecto_IoT.model.User;
import com.example.Proyecto_IoT.repository.MedicionRepository;
import com.example.Proyecto_IoT.repository.UserRepository;
import com.example.Proyecto_IoT.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final DeviceRepository deviceRepository;
    private final MedicionRepository medicionRepository;
    private final UserRepository userRepository;

    public UserDevicesDTO getUserDevices(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("No se proporcionó un ID de usuario válido."));

        // Buscar todos los dispositivos del usuario y mapearlos a DeviceDTO
        List<DeviceDTO> devices = deviceRepository.findAll().stream()
                .filter(d -> d.getUser().getId().equals(userId))
                .map(d -> new DeviceDTO(
                        d.getId(),
                        d.getThingsboardId(),
                        d.getName(),
                        d.getType()
                ))
                .collect(Collectors.toList());

        return UserDevicesDTO.builder()
                .devices(devices)
                .build();
    }

    public UserMedicionesDTO getUserMediciones(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("No se proporcionó un ID de usuario válido."));

        // Buscar todos los dispositivos del usuario y mapearlos a DeviceDTO
        List<MedicionDTO> mediciones = medicionRepository.findAll().stream()
                .filter(m -> m.getUser().getId().equals(userId))
                .map(m -> new MedicionDTO(
                        m.getId(),
                        m.getRutaPdf(),
                        m.getUser().getId()
                ))
                .collect(Collectors.toList());

        return UserMedicionesDTO.builder()
                .mediciones(mediciones)
                .build();
    }
}
