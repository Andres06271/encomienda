package co.edu.unipiloto.encomienda.model;

public class LocalNotification {
    private final long id;
    private final String title;
    private final String message;
    private final long createdAtMillis;
    private boolean read;

    public LocalNotification(long id, String title, String message, long createdAtMillis, boolean read) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.createdAtMillis = createdAtMillis;
        this.read = read;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public long getCreatedAtMillis() {
        return createdAtMillis;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }
}
