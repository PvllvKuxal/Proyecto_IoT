package com.example.Proyecto_IoT.dto.device;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestDeviceDTO implements Serializable {
    private String name;
    private String type;
}
