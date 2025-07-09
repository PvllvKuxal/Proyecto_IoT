package com.example.Proyecto_IoT.service.device;

import com.example.Proyecto_IoT.dto.device.DeviceDTO;
import com.example.Proyecto_IoT.model.Device;
import com.example.Proyecto_IoT.model.User;
import com.example.Proyecto_IoT.repository.DeviceRepository;
import com.example.Proyecto_IoT.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class DeviceService {

    @Value("${thingsboard.api.url}")
    private String tbApiUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ThingsBoardAuthService thingsBoardAuthService;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    public DeviceService(ThingsBoardAuthService thingsBoardAuthService) {
        this.thingsBoardAuthService = thingsBoardAuthService;
    }

    // Registrar/crear dispositivo en ThingsBoard y guardar en BD local
    @Transactional
    public DeviceDTO registerDeviceForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("No se proporcionó un ID de usuario válido."));

        // Buscar todos los dispositivos del usuario y calcular el siguiente número disponible
        List<Device> devices = deviceRepository.findAll().stream()
                .filter(d -> d.getUser().getId().equals(userId))
                .toList();

        int maxNum = devices.stream()
                .map(Device::getName)
                .filter(name -> name.startsWith("user" + userId + "_device"))
                .map(name -> {
                    try {
                        return Integer.parseInt(name.substring(("user" + userId + "_device").length()));
                    } catch (Exception e) {
                        return 0;
                    }
                })
                .max(Integer::compareTo)
                .orElse(0);

        int nextNum = maxNum + 1;
        String deviceName = "user" + userId + "_device" + nextNum;
        String type = "default";

        // Registrar en ThingsBoard
        String url = tbApiUrl + "/api/device";
        HttpHeaders headers = getHeaders();
        Map<String, Object> tbBody = Map.of(
                "name", deviceName,
                "type", type
        );
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(tbBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);
            Map savedDevice = response.getBody();

            if (savedDevice == null || savedDevice.get("id") == null) {
                throw new RuntimeException("No se pudo obtener el ID del dispositivo de ThingsBoard.");
            }
            String thingsboardId = ((Map<String, String>)savedDevice.get("id")).get("id");

            if (thingsboardId == null) {
                throw new RuntimeException("No se pudo obtener el ID del dispositivo de ThingsBoard.");
            }
            // Guardar en la base de datos
            Device localDevice = new Device(
                    thingsboardId,
                    deviceName,
                    type,
                    user
            );
            deviceRepository.save(localDevice);

            return DeviceDTO.builder()
                    .id(localDevice.getId())
                    .thingsboardId(thingsboardId)
                    .name(localDevice.getName())
                    .type(localDevice.getType())
                    .build();

        } catch (HttpClientErrorException.BadRequest e) {
            String errorBody = e.getResponseBodyAsString();
            if (errorBody.contains("Device with such name already exists")) {
                throw new IllegalArgumentException("Ya existe un dispositivo con el nombre: " + deviceName);
            }
            throw new IllegalArgumentException("Error en los datos del dispositivo: " + errorBody);
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Error del cliente HTTP: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new RuntimeException("Error interno al registrar el dispositivo", e);
        }
    }

    public void deleteDevice(String deviceId) {
        // Primero, eliminar el dispositivo de ThingsBoard
        if (deviceId == null || deviceId.isEmpty()) {
            throw new IllegalArgumentException("El ID del dispositivo no puede ser nulo o vacío");
        }

        Device device = deviceRepository.findByThingsboardId(deviceId);
        if (device == null) {
            throw new IllegalArgumentException("Dispositivo no encontrado en la base de datos");
        }
        deviceRepository.delete(device);

        String url = tbApiUrl + "/api/device/" + deviceId;
        HttpHeaders headers = getHeaders();
        HttpEntity<Void> request = new HttpEntity<>(headers);
        try {
            restTemplate.exchange(url, HttpMethod.DELETE, request, Void.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new IllegalArgumentException("Dispositivo no encontrado");
        } catch (Exception e) {
            throw new RuntimeException("Error interno al eliminar el dispositivo");
        }
    }

    public Map<String, Object> getTelemetry(String deviceId) {
        String url = tbApiUrl + "/api/plugins/telemetry/DEVICE/" + deviceId + "/values/timeseries?keys=temperatura,humedad,presion";
        HttpHeaders headers = getHeaders();
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);
        return response.getBody();
    }

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Authorization", "Bearer " + thingsBoardAuthService.getJwtToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}