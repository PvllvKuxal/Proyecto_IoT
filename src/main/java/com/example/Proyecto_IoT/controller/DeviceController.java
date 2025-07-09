package com.example.Proyecto_IoT.controller;

import com.example.Proyecto_IoT.dto.device.DeviceDTO;
import com.example.Proyecto_IoT.dto.medicion.TelemetryDTO;
import com.example.Proyecto_IoT.service.device.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    @Autowired
    private DeviceService deviceService;
    // Crear/registrar un dispositivo en ThingsBoard y en la BD local
    @PostMapping("/register/{userId}")
    public ResponseEntity<?> registerDevice(@PathVariable Long userId) {
        DeviceDTO response = deviceService.registerDeviceForUser(userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }

    @DeleteMapping("/{deviceId}")
    public ResponseEntity<?> deleteDevice(@PathVariable String deviceId) {
        deviceService.deleteDevice(deviceId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("message", "Dispositivo eliminado correctamente"));
    }

    @GetMapping("/{deviceId}/telemetry")
    public TelemetryDTO getTelemetry(@PathVariable String deviceId) {
        return deviceService.getTelemetry(deviceId);
    }


    /*     * Endpoint para recibir datos de telemetría en tiempo real usando Server-Sent Events (SSE)
     * Este endpoint permite a los clientes suscribirse a actualizaciones de telemetría
     * del dispositivo especificado por deviceId.
     */
    @CrossOrigin(origins = "*")
    @GetMapping(value = "/sse/{deviceId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamTelemetry(@PathVariable String deviceId) {
        SseEmitter emitter = new SseEmitter(0L);

        Thread thread = new Thread(() -> {
            try {
                while (true) {
                    try {
                        TelemetryDTO data = deviceService.getTelemetry(deviceId);
                        emitter.send(data);
                    } catch (Exception e) {
                        try {
                            emitter.send(SseEmitter.event().comment("ping"));
                        } catch (Exception ignored) {}
                    }
                    Thread.sleep(1000);
                }
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });
        thread.start();

        emitter.onCompletion(thread::interrupt);
        emitter.onTimeout(() -> {
            thread.interrupt();
            emitter.complete();
        });

        return emitter;
    }
}