package co.edu.unipiloto.encomienda.ui;

import android.content.Intent;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.IOException;
import java.util.List;

import co.edu.unipiloto.encomienda.R;
import co.edu.unipiloto.encomienda.db.DBHelper;

public class CourierMapActivity extends AppCompatActivity {
    private MapView mapView;
    private DBHelper dbHelper;
    private String courierEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courier_map);

        courierEmail = getIntent().getStringExtra("userEmail");
        dbHelper = new DBHelper(this);

        // Configurar OpenStreetMap
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        mapView = findViewById(R.id.mapView);
        mapView.setMultiTouchControls(true);
        
        // Centrar en Bogotá inicialmente
        GeoPoint startPoint = new GeoPoint(4.6097, -74.0817);
        mapView.getController().setCenter(startPoint);
        mapView.getController().setZoom(12.0);

        // Cargar envíos en el mapa
        loadShipmentsOnMap();

        // Botón para cambiar a vista de lista
        findViewById(R.id.fabListView).setOnClickListener(v -> {
            Intent intent = new Intent(this, CourierShipmentListActivity.class);
            intent.putExtra("userEmail", courierEmail);
            startActivity(intent);
            finish();
        });
    }

    private void loadShipmentsOnMap() {
        try (Cursor cursor = dbHelper.getShipmentsAssignedToCourier(courierEmail)) {
            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                    String address = cursor.getString(cursor.getColumnIndexOrThrow("address"));
                    String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));
                    String type = cursor.getString(cursor.getColumnIndexOrThrow("type"));
                    double latitude = cursor.getDouble(cursor.getColumnIndexOrThrow("latitude"));
                    double longitude = cursor.getDouble(cursor.getColumnIndexOrThrow("longitude"));
                    
                    // Usar coordenadas guardadas en lugar de geocoding
                    addMarkerAtLocation(id, address, status, type, latitude, longitude);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error al cargar envíos en el mapa", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void addMarkerAtLocation(int shipmentId, String address, String status, 
                                   String type, double latitude, double longitude) {
        try {
            GeoPoint point = new GeoPoint(latitude, longitude);
            
            Marker marker = new Marker(mapView);
            marker.setPosition(point);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setTitle(address);
            marker.setSnippet("Tipo: " + type + "\nEstado: " + status);
            
            marker.setOnMarkerClickListener((marker1, mapView) -> {
                showChangeStatusDialog(shipmentId, address, status, type);
                return true;
            });
            
            mapView.getOverlays().add(marker);
            mapView.invalidate();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al añadir marcador: " + address, Toast.LENGTH_SHORT).show();
        }
    }

    private void showChangeStatusDialog(int shipmentId, String address, String currentStatus, String type) {
        String[] estados = {"En camino", "Entregado"};
        int currentIndex = "En camino".equals(currentStatus) ? 0 : 1;

        new AlertDialog.Builder(this)
            .setTitle("Cambiar estado del envío")
            .setSingleChoiceItems(estados, currentIndex, null)
            .setPositiveButton("Guardar", (dialog, which) -> {
                int selectedPosition = ((AlertDialog)dialog).getListView().getCheckedItemPosition();
                String newStatus = estados[selectedPosition];
                updateShipmentStatus(shipmentId, address, type, newStatus);
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }

    private void updateShipmentStatus(int shipmentId, String address, String type, String newStatus) {
        try {
            if (dbHelper.updateShipment(shipmentId, address, "", "", type, newStatus)) {
                Toast.makeText(this, "Estado actualizado a: " + newStatus, Toast.LENGTH_SHORT).show();
                // Recargar mapa
                mapView.getOverlays().clear();
                loadShipmentsOnMap();
            } else {
                throw new Exception("Error en la actualización");
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error al actualizar el estado: " + e.getMessage(), 
                Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }
}
