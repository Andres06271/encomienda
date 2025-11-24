package co.edu.unipiloto.encomienda.db;

public class User {
    public int id;
    public String name;
    public String email;
    public String role;

    public User(int id, String name, String email, String role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
    }
}
