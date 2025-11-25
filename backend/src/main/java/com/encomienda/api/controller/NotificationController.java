package com.encomienda.api.controller;

import com.encomienda.api.entity.Notification;
import com.encomienda.api.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationRepository notificationRepository;

    // GET: Listar todas las notificaciones (con filtro opcional por userEmail)
    @GetMapping
    public List<Notification> getAllNotifications(@RequestParam(required = false) String userEmail) {
           if (userEmail == null || userEmail.trim().isEmpty()) {
            return notificationRepository.findAll();
        }
        return notificationRepository.findByUserEmailOrderByCreatedAtDesc(userEmail);
    }

    // GET: Obtener una notificación por ID
    @GetMapping("/{id}")
    public ResponseEntity<Notification> getNotificationById(@PathVariable Long id) {
        return notificationRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST: Crear una nueva notificación
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Notification createNotification(@Validated @RequestBody Notification notification) {
        notification.setId(null); // Asegurar que sea un nuevo registro
        return notificationRepository.save(notification);
    }

    // GET: Contar notificaciones no leídas de un usuario
    @GetMapping("/unread-count")
    public Map<String, Long> getUnreadCount(@RequestParam String userEmail) {
        long count = notificationRepository.countByUserEmailAndReadFlagFalse(userEmail);
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return response;
    }

    // GET: Listar notificaciones no leídas de un usuario
    @GetMapping("/unread")
    public List<Notification> getUnreadNotifications(@RequestParam String userEmail) {
        return notificationRepository.findByUserEmailAndReadFlagFalse(userEmail);
    }

    // PATCH: Marcar una notificación como leída
    @PatchMapping("/{id}/read")
    public ResponseEntity<Notification> markAsRead(@PathVariable Long id) {
        return notificationRepository.findById(id)
                .map(notification -> {
                    notification.setReadFlag(true);
                    Notification updated = notificationRepository.save(notification);
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // PATCH: Marcar todas las notificaciones de un usuario como leídas
    @PatchMapping("/mark-all-read")
    public ResponseEntity<Map<String, Object>> markAllAsRead(@RequestParam String userEmail) {
        List<Notification> unreadNotifications = notificationRepository.findByUserEmailAndReadFlagFalse(userEmail);
        
        for (Notification notification : unreadNotifications) {
            notification.setReadFlag(true);
            notificationRepository.save(notification);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Todas las notificaciones han sido marcadas como leídas");
        response.put("count", unreadNotifications.size());
        
        return ResponseEntity.ok(response);
    }

    // DELETE: Eliminar una notificación
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteNotification(@PathVariable Long id) {
        return notificationRepository.findById(id)
                .map(notification -> {
                    notificationRepository.delete(notification);
                    Map<String, String> response = new HashMap<>();
                    response.put("message", "Notificación eliminada correctamente");
                    response.put("id", id.toString());
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
