package com.example.Proyecto_IoT.dto.device;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceDTO implements Serializable {
    private Long id;
    private String thingsboardId;
    private String name;
    private String type;
}
