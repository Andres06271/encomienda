package co.edu.unipiloto.encomienda.api.dto;

public class CreateShipmentRequest {
    private String address;
    private String status;
    private String type;
    private double latitude;
    private double longitude;
    private String userEmail;
    private String courierEmail;

    public CreateShipmentRequest(String address, String status, String type, double latitude, double longitude, String userEmail, String courierEmail) {
        this.address = address;
        this.status = status;
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
        this.userEmail = userEmail;
        this.courierEmail = courierEmail;
    }

    public String getAddress() { return address; }
    public String getStatus() { return status; }
    public String getType() { return type; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getUserEmail() { return userEmail; }
    public String getCourierEmail() { return courierEmail; }
}
