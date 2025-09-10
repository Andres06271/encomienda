package co.edu.unipiloto.encomienda.model;

public class Shipment {

    private int id;
    private String direccion;
    private String fecha;
    private String hora;
    private String tipo;

    // Constructor vacío (requerido por algunas librerías o adaptadores)
    public Shipment() {
    }

    // Constructor sin ID (para cuando aún no se inserta en BD)
    public Shipment(String direccion, String fecha, String hora, String tipo) {
        this.direccion = direccion;
        this.fecha = fecha;
        this.hora = hora;
        this.tipo = tipo;
    }

    // Constructor con ID (cuando ya existe en BD)
    public Shipment(int id, String direccion, String fecha, String hora, String tipo) {
        this.id = id;
        this.direccion = direccion;
        this.fecha = fecha;
        this.hora = hora;
        this.tipo = tipo;
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
}
