package com.example.Proyecto_IoT.service;

import com.example.Proyecto_IoT.dto.DeviceDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
        String url = tbApiUrl + "/api/device";
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Authorization", "Bearer " + thingsBoardAuthService.getJwtToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<DeviceDTO> request = new HttpEntity<>(device, headers);
        ResponseEntity<DeviceDTO> response = restTemplate.postForEntity(url, request, DeviceDTO.class);

        return response.getBody();
    }

    // Eliminar dispositivo de ThingsBoard
    public void deleteDevice(String deviceId) {
        String url = tbApiUrl + "/api/device/" + deviceId;
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Authorization", "Bearer " + thingsBoardAuthService.getJwtToken());
        HttpEntity<Void> request = new HttpEntity<>(headers);
        restTemplate.exchange(url, HttpMethod.DELETE, request, Void.class);
    }

    // Obtener telemetría del dispositivo
    public Map<String, Object> getTelemetry(String deviceId) {
        String url = tbApiUrl + "/api/plugins/telemetry/DEVICE/" + deviceId + "/values/timeseries?keys=temperatura,humedad,presion";
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Authorization", "Bearer " + thingsBoardAuthService.getJwtToken());
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);
        return response.getBody();
    }
}
