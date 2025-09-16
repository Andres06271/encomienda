package co.edu.unipiloto.encomienda.model;

public class Shipment {

    private int id;
    private String direccion;
    private String fecha;
    private String hora;
    private String tipo;
    private String status; // 🔹 Nuevo campo

    // Constructor vacío (requerido por algunas librerías o adaptadores)
    public Shipment() {
    }

    // Constructor sin ID ni status (para cuando aún no se inserta en BD, usa status por defecto)
    public Shipment(String direccion, String fecha, String hora, String tipo) {
        this.direccion = direccion;
        this.fecha = fecha;
        this.hora = hora;
        this.tipo = tipo;
        this.status = "Pendiente"; // valor por defecto
    }

    // Constructor con ID y sin status (compatibilidad con código viejo)
    public Shipment(int id, String direccion, String fecha, String hora, String tipo) {
        this.id = id;
        this.direccion = direccion;
        this.fecha = fecha;
        this.hora = hora;
        this.tipo = tipo;
        this.status = "Pendiente"; // valor por defecto
    }

    // 🔹 Constructor con ID y Status (cuando ya existe en BD)
    public Shipment(int id, String direccion, String fecha, String hora, String tipo, String status) {
        this.id = id;
        this.direccion = direccion;
        this.fecha = fecha;
        this.hora = hora;
        this.tipo = tipo;
        this.status = status;
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
