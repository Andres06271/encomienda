package co.edu.unipiloto.encomienda.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import co.edu.unipiloto.encomienda.utils.NotificationHelper;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "encomienda.db";
    // Incrementamos la versión para forzar la recreación / migración
    private static final int DATABASE_VERSION = 15; // +1: columna remoteId en shipments

    private final Context context;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
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
        "longitude REAL DEFAULT 0.0," +
        "remoteId INTEGER)";
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

    // Tabla de notificaciones locales
    createNotificationsTable(db);
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

        if (oldVersion < 14) {
            createNotificationsTable(db);
        }

        if (oldVersion < 15) {
            try {
                db.execSQL("ALTER TABLE shipments ADD COLUMN remoteId INTEGER");
            } catch (Exception ignored) {
            }
        }
    }

    private void createNotificationsTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS notifications (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "userEmail TEXT NOT NULL," +
                "title TEXT NOT NULL," +
                "message TEXT NOT NULL," +
                "createdAt INTEGER NOT NULL," +
                "isRead INTEGER NOT NULL DEFAULT 0)");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_notifications_user ON notifications(userEmail, isRead)");
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

    // Obtener todos los usuarios (id, name, email, role)
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id, name, email, role FROM users", null);
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
                String role = cursor.getString(cursor.getColumnIndexOrThrow("role"));
                users.add(new User(id, name, email, role));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return users;
    }

    // Actualizar el rol de un usuario por id
    public boolean updateUserRole(int userId, String newRole) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("role", newRole);
        int rows = db.update("users", values, "id = ?", new String[]{String.valueOf(userId)});
        return rows > 0;
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
        return insertShipmentReturningId(userEmail, address, date, time, type, status, latitude, longitude) != -1;
    }

    // Variante que retorna el ID insertado para poder sincronizar con el backend
    public long insertShipmentReturningId(String userEmail, String address, String date, String time,
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
        return db.insert("shipments", null, values);
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

    public Long getShipmentRemoteId(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT remoteId FROM shipments WHERE id = ?",
                new String[]{String.valueOf(id)});
        try {
            if (cursor.moveToFirst()) {
                if (!cursor.isNull(0)) {
                    return cursor.getLong(0);
                }
            }
            return null;
        } finally {
            cursor.close();
        }
    }

    public boolean setShipmentRemoteId(int id, long remoteId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("remoteId", remoteId);
        int rows = db.update("shipments", values, "id = ?", new String[]{String.valueOf(id)});
        return rows > 0;
    }

    public String getUserEmailForShipment(int shipmentId) {
        try (Cursor cursor = getShipmentById(shipmentId)) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndexOrThrow("userEmail"));
            }
        }
        return null;
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

    // ===================== Notificaciones locales =====================

    public long insertNotification(String userEmail, String title, String message, long createdAtMillis) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("userEmail", userEmail);
        values.put("title", title);
        values.put("message", message);
        values.put("createdAt", createdAtMillis);
        values.put("isRead", 0);
            long notificationId = db.insert("notifications", null, values);
        
            // Mostrar notificación push de Android
            if (notificationId != -1 && context != null) {
                NotificationHelper.showNotification(context, userEmail, title, message, (int) notificationId);
            }
        
            return notificationId;
    }

    public Cursor getNotificationsForUser(String userEmail) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
            "SELECT * FROM notifications WHERE userEmail = ? ORDER BY createdAt DESC",
            new String[]{userEmail}
        );
    }

    public int markNotificationAsRead(long notificationId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("isRead", 1);
        return db.update("notifications", values, "id = ?", new String[]{String.valueOf(notificationId)});
    }

    public int markAllNotificationsAsRead(String userEmail) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("isRead", 1);
        return db.update("notifications", values, "userEmail = ? AND isRead = 0", new String[]{userEmail});
    }

    public int countUnreadNotifications(String userEmail) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
            "SELECT COUNT(*) FROM notifications WHERE userEmail = ? AND isRead = 0",
            new String[]{userEmail}
        );
        try {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
            return 0;
        } finally {
            cursor.close();
        }
    }
}


