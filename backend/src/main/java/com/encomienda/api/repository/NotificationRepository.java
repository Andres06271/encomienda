package com.encomienda.api.repository;

import com.encomienda.api.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    // Buscar notificaciones por email del usuario
    List<Notification> findByUserEmailOrderByCreatedAtDesc(String userEmail);
    
    // Contar notificaciones no leídas por usuario
    long countByUserEmailAndReadFlagFalse(String userEmail);
    
    // Buscar notificaciones no leídas por usuario
    List<Notification> findByUserEmailAndReadFlagFalse(String userEmail);
}
