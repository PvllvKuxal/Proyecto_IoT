package com.example.Proyecto_IoT.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
public class TelemetryDTO {
    private Map<String, Object> data;
    // Puedes agregar más campos si necesitas, pero este Map es flexible para cualquier telemetría

    public TelemetryDTO(Map<String, Object> map) {
        this.data = map;
    }
}
