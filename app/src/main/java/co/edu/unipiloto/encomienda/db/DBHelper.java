package co.edu.unipiloto.encomienda.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "encomienda.db";
    // Incrementamos la versión para forzar la recreación / migración
    private static final int DATABASE_VERSION = 13; // ...cambiado de 12 a 13...

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Modificar tabla usuarios para incluir rol
        String CREATE_USERS_TABLE = "CREATE TABLE users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "email TEXT NOT NULL UNIQUE," +
                "password TEXT NOT NULL," +
                "role TEXT NOT NULL DEFAULT 'user')";
        db.execSQL(CREATE_USERS_TABLE);

        // Tabla envíos (ahora incluye latitude y longitude como REAL)
        String CREATE_SHIPMENTS_TABLE = "CREATE TABLE shipments (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "userEmail TEXT," +
                "courierEmail TEXT," +
                "address TEXT NOT NULL," +
                "date TEXT NOT NULL," +
                "time TEXT NOT NULL," +
                "type TEXT NOT NULL," +
                "status TEXT DEFAULT 'Pendiente'," +
                "latitude REAL DEFAULT 0.0," +
                "longitude REAL DEFAULT 0.0)";
        db.execSQL(CREATE_SHIPMENTS_TABLE);

        // Insertar usuario administrador
        ContentValues adminValues = new ContentValues();
        adminValues.put("name", "Administrador");
        adminValues.put("email", "admin@admin.com");
        adminValues.put("password", "admin123");
        adminValues.put("role", "admin");
        db.insert("users", null, adminValues);

        // Insertar usuario mensajero por defecto
        ContentValues courierValues = new ContentValues();
        courierValues.put("name", "Mensajero");
        courierValues.put("email", "mensajero1@gmail.com");
        courierValues.put("password", "mensajero123");
        courierValues.put("role", "courier");
        db.insert("users", null, courierValues);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 11) {
            // Migrar de isAdmin a role
            db.execSQL("ALTER TABLE users ADD COLUMN role TEXT DEFAULT 'user'");
            db.execSQL("UPDATE users SET role = 'admin' WHERE isAdmin = 1");
            db.execSQL("UPDATE users SET role = 'user' WHERE isAdmin = 0");
        }

        if (oldVersion < 12) {
            try {
                // Añadir columna courierEmail
                db.execSQL("ALTER TABLE shipments ADD COLUMN courierEmail TEXT");
            } catch (Exception e) {
                // Si falla, manejar el error
            }
        }

        if (oldVersion < 13) {
            try {
                // Añadir columnas latitude y longitude
                db.execSQL("ALTER TABLE shipments ADD COLUMN latitude REAL DEFAULT 0.0");
                db.execSQL("ALTER TABLE shipments ADD COLUMN longitude REAL DEFAULT 0.0");
            } catch (Exception e) {
                // ignorar si ya existen
            }
        }
    }

    // Insertar usuario (ahora con parámetro role)
    public boolean insertUser(String name, String email, String password, String role) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("email", email);
        values.put("password", password);
        values.put("role", role);
        long result = db.insert("users", null, values);
        return result != -1;
    }

    // Sobrecarga para compatibilidad
    public boolean insertUser(String name, String email, String password) {
        return insertUser(name, email, password, "user");
    }

    // Validar si el usuario existe
    public boolean checkUserExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM users WHERE email = ?", new String[]{email});
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    // Validar login
    public boolean validateUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM users WHERE email = ? AND password = ?",
                new String[]{email, password}); // Password en texto plano
        boolean valid = cursor.moveToFirst();
        cursor.close();
        return valid;
    }

    // Obtener rol de usuario
    public String getUserRole(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT role FROM users WHERE email = ?", new String[]{email});
        String role = "user";
        if (cursor.moveToFirst()) {
            role = cursor.getString(0);
        }
        cursor.close();
        return role;
    }

    // Validar si es administrador
    public boolean isUserAdmin(String email) {
        return "admin".equals(getUserRole(email));
    }

    // Validar si es mensajero
    public boolean isUserCourier(String email) {
        return "courier".equals(getUserRole(email));
    }

    // Métodos de envíos
    // Mantener compatibilidad: sobrecarga sin lat/lon que delega a la nueva firma
    public boolean insertShipment(String userEmail, String address, String date, String time, String type) {
        return insertShipment(userEmail, address, date, time, type, "Pendiente", 0.0, 0.0);
    }

    public boolean insertShipment(String userEmail, String address, String date, String time,
                                  String type, String status) {
        return insertShipment(userEmail, address, date, time, type, status, 0.0, 0.0);
    }

    // Nueva firma que guarda latitude y longitude
    public boolean insertShipment(String userEmail, String address, String date, String time,
                                  String type, String status, double latitude, double longitude) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("userEmail", userEmail);
        values.put("address", address);
        values.put("date", date);
        values.put("time", time);
        values.put("type", type);
        values.put("status", status);
        values.put("latitude", latitude);
        values.put("longitude", longitude);
        long result = db.insert("shipments", null, values);
        return result != -1;
    }

    // Consultas de envíos
    public Cursor getShipmentsByUser(String userEmail) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM shipments WHERE userEmail = ?", new String[]{userEmail});
    }

    public Cursor getShipmentsByStatus(String userEmail, String status) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM shipments WHERE userEmail = ? AND status = ?",
                new String[]{userEmail, status});
    }

    public Cursor getShipmentById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM shipments WHERE id = ?",
                new String[]{String.valueOf(id)});
    }

    // Obtener TODOS los envíos (solo para admin)
    public Cursor getAllShipments() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM shipments", null);
    }

    // Actualizar y eliminar envíos
    public boolean updateShipment(int id, String address, String date, String time,
                                  String type, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("address", address);
        values.put("date", date);
        values.put("time", time);
        values.put("type", type);
        values.put("status", status);
        int rows = db.update("shipments", values, "id = ?", new String[]{String.valueOf(id)});
        return rows > 0;
    }

    public boolean deleteShipment(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete("shipments", "id = ?", new String[]{String.valueOf(id)});
        return rows > 0;
    }

    // Obtener todos los mensajeros
    public List<String> getAllCouriers() {
        List<String> couriers = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
            "SELECT email FROM users WHERE role = 'courier'", null);
        
        if (cursor.moveToFirst()) {
            do {
                couriers.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return couriers;
    }

    // Asignar envío a mensajero
    public boolean assignShipmentToCourier(int shipmentId, String courierEmail) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("courierEmail", courierEmail);
        values.put("status", "Asignado");
        
        int result = db.update("shipments", values, "id = ?", 
            new String[]{String.valueOf(shipmentId)});
        return result > 0;
    }

    // Obtener envíos asignados a un mensajero
    public Cursor getShipmentsAssignedToCourier(String courierEmail) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
            "SELECT * FROM shipments WHERE courierEmail = ? ORDER BY date, time",
            new String[]{courierEmail}
        );
    }
}


