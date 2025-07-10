package com.example.Proyecto_IoT.controller;

import com.example.Proyecto_IoT.dto.medicion.MedicionDTO;
import com.example.Proyecto_IoT.service.device.MedicionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.File;

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

    @GetMapping("/pdf/{filename}")
    public ResponseEntity<Resource> downloadPdf(@PathVariable String filename) {
        // Ahora buscamos en la carpeta "mediciones_pdf"
        String basePath = "mediciones_pdf";
        File file = new File(basePath, filename);
        if (!file.exists() || !file.isFile()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }
}