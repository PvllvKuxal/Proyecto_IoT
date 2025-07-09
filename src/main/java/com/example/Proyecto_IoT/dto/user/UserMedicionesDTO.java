package com.example.Proyecto_IoT.dto.user;

import com.example.Proyecto_IoT.dto.medicion.MedicionDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserMedicionesDTO {
    private List<MedicionDTO> mediciones;
}
