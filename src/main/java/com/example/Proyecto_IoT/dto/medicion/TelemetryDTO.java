package com.example.Proyecto_IoT.dto.medicion;

import lombok.*;

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
