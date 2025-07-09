package com.example.Proyecto_IoT.dto.device;

import lombok.*;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TelemetryDTO {
    private String timeStamp;
    private String temperatura;
    private String humedad;
    private String presion;
}
