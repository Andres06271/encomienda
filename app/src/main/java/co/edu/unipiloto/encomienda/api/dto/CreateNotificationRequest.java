package co.edu.unipiloto.encomienda.api.dto;

public class CreateNotificationRequest {
    private String userEmail;
    private String title;
    private String body;

    public CreateNotificationRequest(String userEmail, String title, String body) {
        this.userEmail = userEmail;
        this.title = title;
        this.body = body;
    }
    public String getUserEmail() { return userEmail; }
    public String getTitle() { return title; }
    public String getBody() { return body; }
}
