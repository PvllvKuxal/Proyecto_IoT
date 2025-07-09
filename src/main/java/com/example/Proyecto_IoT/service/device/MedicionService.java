package com.example.Proyecto_IoT.service.device;

import com.example.Proyecto_IoT.dto.device.MedicionDTO;
import com.example.Proyecto_IoT.dto.device.TelemetryDTO;
import com.example.Proyecto_IoT.model.Device;
import com.example.Proyecto_IoT.model.Medicion;
import com.example.Proyecto_IoT.model.User;
import com.example.Proyecto_IoT.repository.DeviceRepository;
import com.example.Proyecto_IoT.repository.MedicionRepository;
import com.example.Proyecto_IoT.util.PdfGenerator;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;

@Service
public class MedicionService {

    @Autowired
    private DeviceService deviceService;
    @Autowired
    private DeviceRepository deviceRepository;
    @Autowired
    private PdfGenerator pdfGenerator;
    @Autowired
    private MedicionRepository medicionRepository;

    // Sesiones activas: key = deviceId-userId, value = MedicionSession
    private final Map<String, MedicionSession> sesionesActivas = new ConcurrentHashMap<>();

    public String iniciarMedicion(String deviceId) {
        User user = getUserFromDevice(deviceId); // Asegurarse de que el dispositivo existe y obtener el userId
        String key = deviceId + "-" + user.getId();
        if (sesionesActivas.containsKey(key)) {
            throw new RuntimeException("Ya hay una medición activa para este dispositivo y usuario.");
        }
        MedicionSession session = new MedicionSession(deviceId, user.getId(), deviceService);
        sesionesActivas.put(key, session);
        session.start();
        return session.getId();
    }

    public MedicionDTO detenerMedicion(String deviceId) {
        User user = getUserFromDevice(deviceId); // Asegurarse de que el dispositivo existe y obtener el userId
        Device device = deviceRepository.findByThingsboardId(deviceId);
        if (device == null) {
            throw new RuntimeException("Dispositivo no encontrado.");
        }
        String key = deviceId + "-" + user.getId();

        MedicionSession session = sesionesActivas.remove(key);
        if (session == null) throw new RuntimeException("No hay medición activa para este dispositivo y usuario.");
        session.stop();

        List<TelemetryDTO> datos = session.getDatosRecolectados();

        if (!Objects.equals(session.getUserId(), user.getId())) {
            throw new RuntimeException("El usuario no coincide con el de la sesión de medición.");
        }

        String rutaPdf = pdfGenerator.generar(datos, user, device);
        Medicion medicion = new Medicion(rutaPdf, user);
        medicionRepository.save(medicion);
        return new MedicionDTO(medicion.getId(), medicion.getRutaPdf(), medicion.getUser().getId());
    }

    private User getUserFromDevice(String deviceId) {
        Device device = deviceRepository.findByThingsboardId(deviceId);
        if (device == null) {
            throw new RuntimeException("Dispositivo no encontrado.");
        }
        return device.getUser();
    }

    public MedicionDTO getMedicion(Long medicionId) {
        Medicion medicion = medicionRepository.findById(medicionId)
                .orElseThrow(() -> new RuntimeException("Medición no encontrada."));
        return new MedicionDTO(medicion.getId(), medicion.getRutaPdf(), medicion.getUser().getId());
    }

    /**
     * Obtiene los datos recolectados de una sesión de medición activa.
     * Clase interna que maneja la sesión de medición.
     * Cada sesión recolecta datos del dispositivo a intervalos regulares.
     * Los datos se almacenan en una lista y se pueden detener en cualquier momento.
     */
    private static class MedicionSession {
        @Getter
        private final String id = UUID.randomUUID().toString();
        private final String deviceId;
        @Getter
        private final Long userId;
        private final DeviceService deviceService;
        @Getter
        private final List<TelemetryDTO> datosRecolectados = new CopyOnWriteArrayList<>();
        private volatile boolean running = false;
        private Thread thread;

        public MedicionSession(String deviceId, Long userId, DeviceService deviceService) {
            this.deviceId = deviceId;
            this.userId = userId;
            this.deviceService = deviceService;
        }

        public void start() {
            running = true;
            thread = new Thread(() -> {
                while (running) {
                    try {
                        TelemetryDTO data = deviceService.getTelemetry(deviceId);
                        datosRecolectados.add(data);
                        System.out.println("Datos recolectados: " + data);
                        Thread.sleep(1000); // 1 segundo entre muestras
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    } catch (Exception ignored) {}
                }
            });
            thread.start();
        }

        public void stop() {
            running = false;
            if (thread != null) thread.interrupt();
        }

    }
}