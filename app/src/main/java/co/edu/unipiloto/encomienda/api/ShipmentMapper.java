package co.edu.unipiloto.encomienda.api;

import co.edu.unipiloto.encomienda.api.dto.RemoteShipment;
import co.edu.unipiloto.encomienda.model.Shipment;

public class ShipmentMapper {

    // Convierte RemoteShipment del backend a tu modelo local Shipment (simplificado)
    public static Shipment toLocal(RemoteShipment remote) {
        if (remote == null) return null;
        // Usa createdAt para fecha/hora si quisieras parsear; aquí se deja vacío para simplicidad
        return new Shipment(
                remote.getId() == null ? 0 : remote.getId().intValue(),
                remote.getAddress(),
                "", // fecha (opcional)
                "", // hora (opcional)
                remote.getType(),
                remote.getStatus()
        );
    }
}
