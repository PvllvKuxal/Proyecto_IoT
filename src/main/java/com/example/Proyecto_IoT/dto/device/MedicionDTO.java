package com.example.Proyecto_IoT.dto.device;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicionDTO {
    private Long id;
    private String rutaPdf;
    private Long userId;
}