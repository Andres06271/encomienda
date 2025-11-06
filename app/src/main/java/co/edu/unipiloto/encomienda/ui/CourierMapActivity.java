package co.edu.unipiloto.encomienda.ui;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import co.edu.unipiloto.encomienda.R;
import co.edu.unipiloto.encomienda.db.DBHelper;

public class CourierMapActivity extends AppCompatActivity {

    private static final int MAX_OPTIMIZED_WAYPOINTS = 9; // Google Maps allows up to 9 optimized waypoints plus destination.

    private MapView mapView;
    private DBHelper dbHelper;
    private String courierEmail;
    private FloatingActionButton routePlannerFab;

    private final List<ShipmentLocation> assignedShipments = new ArrayList<>();

    private static final class ShipmentLocation {
        final int id;
        final String address;
        final String status;
        final String type;
        final double latitude;
        final double longitude;

        ShipmentLocation(int id, String address, String status, String type,
                         double latitude, double longitude) {
            this.id = id;
            this.address = address;
            this.status = status;
            this.type = type;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        boolean hasValidCoordinates() {
            return !(Double.compare(latitude, 0.0) == 0 && Double.compare(longitude, 0.0) == 0);
        }
    }

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

        routePlannerFab = findViewById(R.id.fabRoutePlanner);
        if (routePlannerFab != null) {
            routePlannerFab.setOnClickListener(v -> openRoutePlanner());
        }
    }

    private void loadShipmentsOnMap() {
        mapView.getOverlays().clear();
        assignedShipments.clear();

        try (Cursor cursor = dbHelper.getShipmentsAssignedToCourier(courierEmail)) {
            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                    String address = cursor.getString(cursor.getColumnIndexOrThrow("address"));
                    String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));
                    String type = cursor.getString(cursor.getColumnIndexOrThrow("type"));
                    double latitude = cursor.getDouble(cursor.getColumnIndexOrThrow("latitude"));
                    double longitude = cursor.getDouble(cursor.getColumnIndexOrThrow("longitude"));
                    
                    assignedShipments.add(new ShipmentLocation(id, address, status, type, latitude, longitude));

                    // Usar coordenadas guardadas en lugar de geocoding
                    addMarkerAtLocation(id, address, status, type, latitude, longitude);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error al cargar envíos en el mapa", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void openRoutePlanner() {
        List<ShipmentLocation> validShipments = new ArrayList<>();
        for (ShipmentLocation shipment : assignedShipments) {
            if (shipment.hasValidCoordinates()) {
                validShipments.add(shipment);
            }
        }

        if (validShipments.isEmpty()) {
            showToast(R.string.route_plan_no_shipments);
            return;
        }

        int maxStops = MAX_OPTIMIZED_WAYPOINTS + 1;
        boolean trimmed = false;
        List<ShipmentLocation> shipmentsForRoute = new ArrayList<>(validShipments);
        if (shipmentsForRoute.size() > maxStops) {
            shipmentsForRoute = new ArrayList<>(shipmentsForRoute.subList(0, maxStops));
            trimmed = true;
        }

        Uri gmmIntentUri = buildRouteUri(shipmentsForRoute);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);

        Log.d("CourierMap", "Primary map intent resolved by: " + mapIntent.resolveActivity(getPackageManager()));

        try {
            if (trimmed) {
                showToast(getString(R.string.route_plan_limit_warning, maxStops));
            }
            startActivity(mapIntent);
            return;
        } catch (ActivityNotFoundException primaryException) {
            Log.w("CourierMap", "Primary map intent failed", primaryException);
        }

        Intent fallbackIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        Log.d("CourierMap", "Fallback map intent resolved by: " + fallbackIntent.resolveActivity(getPackageManager()));
        try {
            if (trimmed) {
                showToast(getString(R.string.route_plan_limit_warning, maxStops));
            }
            startActivity(fallbackIntent);
        } catch (ActivityNotFoundException secondaryException) {
            Log.w("CourierMap", "Fallback map intent failed", secondaryException);
            showToast(R.string.route_plan_no_maps);
        }
    }

    private Uri buildRouteUri(List<ShipmentLocation> shipments) {
        Uri.Builder builder = new Uri.Builder()
            .scheme("https")
            .authority("www.google.com")
            .appendPath("maps")
            .appendPath("dir")
            .appendPath("")
            .appendQueryParameter("api", "1")
            .appendQueryParameter("origin", "Current+Location")
            .appendQueryParameter("travelmode", "driving");

        ShipmentLocation destination = shipments.get(shipments.size() - 1);
        builder.appendQueryParameter("destination", formatLatLng(destination));

        if (shipments.size() > 1) {
            StringBuilder waypointBuilder = new StringBuilder("optimize:true");
            for (int i = 0; i < shipments.size() - 1; i++) {
                waypointBuilder.append("|").append(formatLatLng(shipments.get(i)));
            }
            builder.appendQueryParameter("waypoints", waypointBuilder.toString());
        }

        return Uri.parse(builder.build().toString());
    }

    private String formatLatLng(ShipmentLocation location) {
        return String.format(Locale.US, "%.6f,%.6f", location.latitude, location.longitude);
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

    private void showToast(@StringRes int resId) {
        Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
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
