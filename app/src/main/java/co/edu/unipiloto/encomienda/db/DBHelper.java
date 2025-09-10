package co.edu.unipiloto.encomienda.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "encomienda.db";
    private static final int DATABASE_VERSION = 2; // actualizado a 2 para shipments

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

        // Tabla envíos
        String CREATE_SHIPMENTS_TABLE = "CREATE TABLE shipments (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "userEmail TEXT," +
                "address TEXT NOT NULL," +
                "date TEXT NOT NULL," +
                "time TEXT NOT NULL," +
                "type TEXT NOT NULL)";
        db.execSQL(CREATE_SHIPMENTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS users");
        db.execSQL("DROP TABLE IF EXISTS shipments");
        onCreate(db);
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

    // Insertar envío
    public boolean insertShipment(String userEmail, String address, String date, String time, String type) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("userEmail", userEmail);
        values.put("address", address);
        values.put("date", date);
        values.put("time", time);
        values.put("type", type);

        long result = db.insert("shipments", null, values);
        return result != -1;
    }

    // 🔹 Nuevo: obtener envíos por usuario
    public Cursor getShipmentsByUser(String userEmail) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM shipments WHERE userEmail = ?", new String[]{userEmail});
    }
}
