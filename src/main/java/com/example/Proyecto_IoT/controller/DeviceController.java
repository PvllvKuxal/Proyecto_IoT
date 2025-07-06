package com.example.Proyecto_IoT.controller;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import com.example.Proyecto_IoT.service.DeviceService;
import com.example.Proyecto_IoT.dto.DeviceDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    @Autowired
    private DeviceService deviceService;

    // Crear/registrar un dispositivo en ThingsBoard
    @PostMapping
    public DeviceDTO registerDevice(@RequestBody DeviceDTO deviceDTO) {
        try {
            return deviceService.registerDevice(deviceDTO);
        } catch (IllegalArgumentException e) {
            // Errores de validación o dispositivo duplicado
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (Exception e) {
            // Otros errores internos
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Error al registrar el dispositivo", e);
        }
    }

    @DeleteMapping("/{deviceId}")
    public void deleteDevice(@PathVariable String deviceId) {
        try {
            deviceService.deleteDevice(deviceId);
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Error al eliminar el dispositivo", e);
        }
    }

    @GetMapping("/{deviceId}/telemetry")
    public Map<String, Object> getTelemetry(@PathVariable String deviceId) {
        try {
            return deviceService.getTelemetry(deviceId);
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Error al obtener la telemetría", e);
        }
    }

    @CrossOrigin(origins = "*")
    @GetMapping(value = "/sse/{deviceId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamTelemetry(@PathVariable String deviceId) {
        SseEmitter emitter = new SseEmitter(0L);

        Thread thread = new Thread(() -> {
            try {
                while (true) {
                    try {
                        Map<String, Object> data = deviceService.getTelemetry(deviceId);
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