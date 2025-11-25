package co.edu.unipiloto.encomienda.api.dto;

public class RemoteShipment {
    private Long id;
    private String address;
    private String status;
    private String type;
    private double latitude;
    private double longitude;
    private String userEmail;
    private String courierEmail;
    private String createdAt; // ISO string
    private String updatedAt; // ISO string

    public Long getId() { return id; }
    public String getAddress() { return address; }
    public String getStatus() { return status; }
    public String getType() { return type; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getUserEmail() { return userEmail; }
    public String getCourierEmail() { return courierEmail; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }
}
