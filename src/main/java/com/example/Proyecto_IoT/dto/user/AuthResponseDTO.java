package com.example.Proyecto_IoT.dto.user;

import com.example.Proyecto_IoT.dto.device.DeviceDTO;

import java.util.List;

public class AuthResponseDTO {
    private String token;
    private List<DeviceDTO> devices;
    private List<String> pdfPaths;

    public AuthResponseDTO() {}

    public AuthResponseDTO(String token, List<DeviceDTO> devices, List<String> pdfPaths) {
        this.token = token;
        this.devices = devices;
        this.pdfPaths = pdfPaths;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public List<DeviceDTO> getDevices() {
        return devices;
    }

    public void setDevices(List<DeviceDTO> devices) {
        this.devices = devices;
    }

    public List<String> getPdfPaths() {
        return pdfPaths;
    }

    public void setPdfPaths(List<String> pdfPaths) {
        this.pdfPaths = pdfPaths;
    }
} 