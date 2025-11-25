package co.edu.unipiloto.encomienda.api.dto;

public class RemoteNotification {
    private Long id;
    private String userEmail;
    private String title;
    private String body;
    private boolean readFlag;
    private String createdAt;

    public Long getId() { return id; }
    public String getUserEmail() { return userEmail; }
    public String getTitle() { return title; }
    public String getBody() { return body; }
    public boolean isReadFlag() { return readFlag; }
    public String getCreatedAt() { return createdAt; }
}
