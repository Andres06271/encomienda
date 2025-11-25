package com.encomienda.api.repository;

import com.encomienda.api.entity.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    
    // Buscar envíos por email del courier, ordenados por fecha de creación descendente
    List<Shipment> findByCourierEmailOrderByCreatedAtDesc(String courierEmail);
    
    // Buscar envíos por email del usuario
    List<Shipment> findByUserEmail(String userEmail);
    
    // Buscar envíos por estado
    List<Shipment> findByStatus(String status);
}
