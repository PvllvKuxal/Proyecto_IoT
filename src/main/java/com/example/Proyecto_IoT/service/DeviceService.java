package com.example.Proyecto_IoT.service;

import com.example.Proyecto_IoT.dto.device.RequestDeviceDTO;
import com.example.Proyecto_IoT.dto.device.DeviceDTO;
import com.example.Proyecto_IoT.model.Device;
import com.example.Proyecto_IoT.repository.DeviceRepository;
import com.example.Proyecto_IoT.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;

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
    public DeviceDTO registerDevice(RequestDeviceDTO request, Long userId) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("El campo 'name' del dispositivo no puede ser nulo o vacío.");
        }
        String type = (request.getType() == null || request.getType().trim().isEmpty()) ? "default" : request.getType();

        var user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("No se proporcionó un ID de usuario válido."));

        String url = tbApiUrl + "/api/device";
        HttpHeaders headers = getHeaders();

        // Construir el body para ThingsBoard
        Map<String, Object> tbBody = Map.of(
            "name", request.getName(),
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
            // Guardar en la base de datos
            Device localDevice = new Device(
                thingsboardId,
                request.getName(),
                type,
                user
            );
            Device saved = deviceRepository.save(localDevice);
            return new DeviceDTO(
                saved.getId(),
                saved.getThingsboardId(),
                saved.getName(),
                saved.getType(),
                saved.getUser().getId()
            );
        } catch (HttpClientErrorException.BadRequest e) {
            String errorBody = e.getResponseBodyAsString();
            if (errorBody.contains("Device with such name already exists")) {
                throw new IllegalArgumentException("Ya existe un dispositivo con el nombre: " + request.getName());
            }
            throw new IllegalArgumentException("Error en los datos del dispositivo: " + errorBody);
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Error del cliente HTTP: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new RuntimeException("Error interno al registrar el dispositivo", e);
        }
    }

    public void deleteDevice(String deviceId) {
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