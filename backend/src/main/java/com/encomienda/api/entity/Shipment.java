package com.encomienda.api.entity;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.Instant;

@Entity
@Table(name = "shipments")
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "La dirección es obligatoria")
    @Column(nullable = false)
    private String address;

    @NotBlank(message = "El estado es obligatorio")
    @Column(nullable = false)
    private String status; // "En camino", "Entregado", "Pendiente"

    @NotBlank(message = "El tipo es obligatorio")
    @Column(nullable = false)
    private String type; // "Paquete", "Sobre", "Caja"

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    @Email(message = "Email del usuario no válido")
    @NotBlank(message = "El email del usuario es obligatorio")
    @Column(nullable = false)
    private String userEmail;

    @Email(message = "Email del courier no válido")
    @Column(nullable = true)
    private String courierEmail;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    // Constructors
    public Shipment() {
    }

    public Shipment(String address, String status, String type, double latitude, double longitude, 
                   String userEmail, String courierEmail) {
        this.address = address;
        this.status = status;
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
        this.userEmail = userEmail;
        this.courierEmail = courierEmail;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getCourierEmail() {
        return courierEmail;
    }

    public void setCourierEmail(String courierEmail) {
        this.courierEmail = courierEmail;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
