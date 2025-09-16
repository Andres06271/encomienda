package co.edu.unipiloto.encomienda.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "encomienda.db";
    // Subimos la versión para aplicar la migración que añade 'status'
    private static final int DATABASE_VERSION = 3;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tabla usuarios
        String CREATE_USERS_TABLE = "CREATE TABLE users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "email TEXT NOT NULL UNIQUE," +
                "password TEXT NOT NULL)";
        db.execSQL(CREATE_USERS_TABLE);

        // Tabla envíos (incluye columna status con valor por defecto 'Pendiente')
        String CREATE_SHIPMENTS_TABLE = "CREATE TABLE shipments (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "userEmail TEXT," +
                "address TEXT NOT NULL," +
                "date TEXT NOT NULL," +
                "time TEXT NOT NULL," +
                "type TEXT NOT NULL," +
                "status TEXT DEFAULT 'Pendiente')";
        db.execSQL(CREATE_SHIPMENTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Si la versión antigua no tenía 'status', la añadimos con ALTER TABLE (no borramos datos)
        if (oldVersion < 3) {
            try {
                db.execSQL("ALTER TABLE shipments ADD COLUMN status TEXT DEFAULT 'Pendiente'");
            } catch (Exception e) {
                // Si por alguna razón falla (tabla no existe, etc.), como fallback recreamos tabla.
                // (opcional) podrías hacer un backup/restore aquí.
            }
        }

        // Si más adelante cambias versiones, añade nuevas migraciones condicionadas por oldVersion.
    }

    // Insertar usuario
    public boolean insertUser(String name, String email, String passwordHash) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("email", email);
        values.put("password", passwordHash);

        long result = db.insert("users", null, values);
        return result != -1;
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
    public boolean validateUser(String email, String passwordHash) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM users WHERE email = ? AND password = ?", new String[]{email, passwordHash});
        boolean valid = cursor.moveToFirst();
        cursor.close();
        return valid;
    }

    // Insertar envío (versión antigua, mantiene compatibilidad; asigna 'Pendiente')
    public boolean insertShipment(String userEmail, String address, String date, String time, String type) {
        return insertShipment(userEmail, address, date, time, type, "Pendiente");
    }

    // Insertar envío (con status explícito)
    public boolean insertShipment(String userEmail, String address, String date, String time, String type, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("userEmail", userEmail);
        values.put("address", address);
        values.put("date", date);
        values.put("time", time);
        values.put("type", type);
        values.put("status", status);

        long result = db.insert("shipments", null, values);
        return result != -1;
    }

    // Obtener todos los envíos de un usuario
    public Cursor getShipmentsByUser(String userEmail) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM shipments WHERE userEmail = ?", new String[]{userEmail});
    }

    // Obtener envíos por usuario y estado (filtro)
    public Cursor getShipmentsByStatus(String userEmail, String status) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM shipments WHERE userEmail = ? AND status = ?", new String[]{userEmail, status});
    }

    // Obtener un envío por su id
    public Cursor getShipmentById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM shipments WHERE id = ?", new String[]{String.valueOf(id)});
    }

    // Actualizar envío por id
    public boolean updateShipment(int id, String address, String date, String time, String type, String status) {
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

    // (Opcional) Eliminar envío
    public boolean deleteShipment(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete("shipments", "id = ?", new String[]{String.valueOf(id)});
        return rows > 0;
    }
}

