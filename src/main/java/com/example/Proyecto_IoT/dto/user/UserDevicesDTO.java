package com.example.Proyecto_IoT.dto.user;

import com.example.Proyecto_IoT.dto.device.DeviceDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDevicesDTO {
    private List<DeviceDTO> devices;
}
