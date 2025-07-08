package com.example.Proyecto_IoT.repository;

import com.example.Proyecto_IoT.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceRepository extends JpaRepository<Device, Long> {
    boolean existsByThingsboardId(String thingsboardId);
    Device findByThingsboardId(String thingsboardId);
}

