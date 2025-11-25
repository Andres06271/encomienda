package co.edu.unipiloto.encomienda.api.dto;

public class UpdateStatusRequest {
    private String status;

    public UpdateStatusRequest(String status) { this.status = status; }
    public String getStatus() { return status; }
}
