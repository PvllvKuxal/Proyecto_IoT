package com.example.Proyecto_IoT.dto.device;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TelemetryDTO {
    private Map<String, Object> data;
}
