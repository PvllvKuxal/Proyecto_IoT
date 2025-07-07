package com.example.Proyecto_IoT.service;

import com.example.Proyecto_IoT.dto.DeviceDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import java.util.Map;
@Service
public class DeviceService {

    @Value("${thingsboard.api.url}")
    private String tbApiUrl;

    @Value("${thingsboard.username}")
    private String username;

    @Value("${thingsboard.password}")
    private String password;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ThingsBoardAuthService thingsBoardAuthService;

    @Autowired
    public DeviceService(ThingsBoardAuthService thingsBoardAuthService) {
        this.thingsBoardAuthService = thingsBoardAuthService;
    }

    // Registrar/crear dispositivo en ThingsBoard
    public DeviceDTO registerDevice(DeviceDTO device) {
        if (device.getName() == null || device.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("El campo 'name' del dispositivo no puede ser nulo o vacío.");
        }
        if (device.getType() == null || device.getType().trim().isEmpty()) {
            device.setType("default"); // Valor por defecto requerido por ThingsBoard
        }

        String url = tbApiUrl + "/api/device";
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Authorization", "Bearer " + thingsBoardAuthService.getJwtToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<DeviceDTO> request = new HttpEntity<>(device, headers);

        try {
            ResponseEntity<DeviceDTO> response = restTemplate.postForEntity(url, request, DeviceDTO.class);
            DeviceDTO savedDevice = response.getBody();

            // --- Configuración automática de credenciales MQTT_BASIC ---
            if (savedDevice != null && savedDevice.getId() != null && savedDevice.getId().getId() != null) {
                String deviceId = savedDevice.getId().getId();
                String credentialsUrl = tbApiUrl + "/api/device/" + deviceId + "/credentials";

                // Puedes personalizar estos valores o generarlos aleatoriamente
                String clientId = "client-" + deviceId;
                String userName = "user-" + deviceId;
                String password = "pass-" + deviceId;

                String credentialsValue = String.format("{\"clientId\":\"%s\",\"userName\":\"%s\",\"password\":\"%s\"}", clientId, userName, password);

                Map<String, Object> credBody = new java.util.HashMap<>();
                credBody.put("credentialsType", "MQTT_BASIC");
                credBody.put("credentialsId", clientId);
                credBody.put("credentialsValue", credentialsValue);

                HttpEntity<Map<String, Object>> credRequest = new HttpEntity<>(credBody, headers);
                restTemplate.exchange(credentialsUrl, HttpMethod.PUT, credRequest, Void.class);

                // Puedes devolver los datos MQTT en el DeviceDTO usando additionalInfo
                Map<String, Object> mqttInfo = new java.util.HashMap<>();
                mqttInfo.put("clientId", clientId);
                mqttInfo.put("userName", userName);
                mqttInfo.put("password", password);
                savedDevice.setAdditionalInfo(mqttInfo);
            }

            return savedDevice;
        } catch (HttpClientErrorException.BadRequest e) {
            // Capturar específicamente errores 400 de ThingsBoard
            String errorBody = e.getResponseBodyAsString();
            if (errorBody.contains("Device with such name already exists")) {
                throw new IllegalArgumentException("Ya existe un dispositivo con el nombre: " + device.getName());
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
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Authorization", "Bearer " + thingsBoardAuthService.getJwtToken());
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
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Authorization", "Bearer " + thingsBoardAuthService.getJwtToken());
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);
        return response.getBody();
    }
}