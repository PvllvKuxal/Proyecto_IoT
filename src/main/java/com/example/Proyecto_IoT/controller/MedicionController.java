package com.example.Proyecto_IoT.controller;

import com.example.Proyecto_IoT.dto.medicion.MedicionDTO;
import com.example.Proyecto_IoT.service.device.MedicionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mediciones")
public class MedicionController {

    @Autowired
    private MedicionService medicionService;

    @PostMapping("/{deviceId}/iniciar")
    public ResponseEntity<String> iniciarMedicion(@PathVariable String deviceId) {
        String medicionId = medicionService.iniciarMedicion(deviceId);
        return ResponseEntity.ok(medicionId);
    }

    @PostMapping("/{deviceId}/detener")
    public ResponseEntity<MedicionDTO> detenerMedicion(@PathVariable String deviceId) {
        MedicionDTO medicion = medicionService.detenerMedicion(deviceId);
        return ResponseEntity.ok(medicion);
    }

    @GetMapping("/{medicionId}")
    public ResponseEntity<MedicionDTO> getMedicion(@PathVariable Long medicionId) {
        MedicionDTO medicion = medicionService.getMedicion(medicionId);
        return ResponseEntity.ok(medicion);
    }
}