package com.example.Proyecto_IoT.repository;

import com.example.Proyecto_IoT.model.Medicion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicionRepository extends JpaRepository<Medicion, Long> {
}
