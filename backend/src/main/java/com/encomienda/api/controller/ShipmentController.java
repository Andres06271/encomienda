package com.encomienda.api.controller;

import com.encomienda.api.entity.Shipment;
import com.encomienda.api.repository.ShipmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/shipments")
public class ShipmentController {

    @Autowired
    private ShipmentRepository shipmentRepository;

    // GET: Listar todos los envíos (con filtro opcional por courierEmail)
    @GetMapping
    public List<Shipment> getAllShipments(@RequestParam(required = false) String courierEmail) {
           if (courierEmail == null || courierEmail.trim().isEmpty()) {
            return shipmentRepository.findAll();
        }
        return shipmentRepository.findByCourierEmailOrderByCreatedAtDesc(courierEmail);
    }

    // GET: Obtener un envío por ID
    @GetMapping("/{id}")
    public ResponseEntity<Shipment> getShipmentById(@PathVariable Long id) {
        return shipmentRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST: Crear un nuevo envío
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Shipment createShipment(@Validated @RequestBody Shipment shipment) {
        shipment.setId(null); // Asegurar que sea un nuevo registro
        return shipmentRepository.save(shipment);
    }

    // PUT: Actualizar un envío completo
    @PutMapping("/{id}")
    public ResponseEntity<Shipment> updateShipment(@PathVariable Long id, 
                                                    @Validated @RequestBody Shipment shipmentDetails) {
        return shipmentRepository.findById(id)
                .map(shipment -> {
                    shipment.setAddress(shipmentDetails.getAddress());
                    shipment.setStatus(shipmentDetails.getStatus());
                    shipment.setType(shipmentDetails.getType());
                    shipment.setLatitude(shipmentDetails.getLatitude());
                    shipment.setLongitude(shipmentDetails.getLongitude());
                    shipment.setUserEmail(shipmentDetails.getUserEmail());
                    shipment.setCourierEmail(shipmentDetails.getCourierEmail());
                    Shipment updatedShipment = shipmentRepository.save(shipment);
                    return ResponseEntity.ok(updatedShipment);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // PATCH: Actualizar solo el estado de un envío
    @PatchMapping("/{id}/status")
    public ResponseEntity<Shipment> updateShipmentStatus(@PathVariable Long id, 
                                                          @RequestBody Map<String, String> updates) {
        return shipmentRepository.findById(id)
                .map(shipment -> {
                    if (updates.containsKey("status")) {
                        shipment.setStatus(updates.get("status"));
                    }
                    Shipment updatedShipment = shipmentRepository.save(shipment);
                    return ResponseEntity.ok(updatedShipment);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE: Eliminar un envío
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteShipment(@PathVariable Long id) {
        return shipmentRepository.findById(id)
                .map(shipment -> {
                    shipmentRepository.delete(shipment);
                    Map<String, String> response = new HashMap<>();
                    response.put("message", "Envío eliminado correctamente");
                    response.put("id", id.toString());
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // GET: Buscar envíos por email de usuario
    @GetMapping("/user/{userEmail}")
    public List<Shipment> getShipmentsByUser(@PathVariable String userEmail) {
        return shipmentRepository.findByUserEmail(userEmail);
    }

    // GET: Buscar envíos por estado
    @GetMapping("/status/{status}")
    public List<Shipment> getShipmentsByStatus(@PathVariable String status) {
        return shipmentRepository.findByStatus(status);
    }
}
