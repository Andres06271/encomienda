package co.edu.unipiloto.encomienda.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import co.edu.unipiloto.encomienda.R;
import co.edu.unipiloto.encomienda.db.DBHelper;

public class ShipmentActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST = 1001;

    private EditText etAddress;
    private EditText etDate;
    private EditText etTime;
    private EditText etType;
    private MaterialButton btnSaveShipment;
    private MaterialButton btnUseCurrentLocation;
    private MapView mapPreview;
    private CardView mapPreviewCard;

    private final ExecutorService geocodeExecutor = Executors.newSingleThreadExecutor();
    private Runnable pendingLocationPermissionAction;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    private DBHelper dbHelper;
    private String userEmail;
    private GeoPoint selectedLocation;
    private double selectedLat = 0.0;
    private double selectedLon = 0.0;
    private boolean locationSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipment);

        dbHelper = new DBHelper(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        etAddress = findViewById(R.id.etAddress);
        etDate = findViewById(R.id.etDate);
        etTime = findViewById(R.id.etTime);
        etType = findViewById(R.id.etType);
        btnSaveShipment = findViewById(R.id.btnSaveShipment);
        btnUseCurrentLocation = findViewById(R.id.btnUseCurrentLocation);
        mapPreview = findViewById(R.id.mapPreview);
        mapPreviewCard = findViewById(R.id.mapPreviewCard);

        userEmail = getIntent().getStringExtra("userEmail");

        Configuration.getInstance().load(this,
            PreferenceManager.getDefaultSharedPreferences(this));

        mapPreview.setMultiTouchControls(true);
        mapPreview.getController().setZoom(15.0);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                fusedLocationClient.removeLocationUpdates(this);
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    onLocationObtained(location);
                } else {
                    setLocationFetchInProgress(false);
                    Toast.makeText(ShipmentActivity.this,
                        R.string.location_not_found, Toast.LENGTH_SHORT).show();
                }
            }
        };

        etAddress.setOnClickListener(v -> ensureLocationPermission(this::openMapDialog));
        View mapIcon = findViewById(R.id.ivMapIcon);
        if (mapIcon != null) {
            mapIcon.setOnClickListener(v -> ensureLocationPermission(this::openMapDialog));
        }

        btnUseCurrentLocation.setOnClickListener(v -> ensureLocationPermission(this::fetchCurrentLocation));
        btnSaveShipment.setOnClickListener(v -> saveShipment());
    }

    @Override
    protected void onDestroy() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
        geocodeExecutor.shutdownNow();
        super.onDestroy();
    }

    private void saveShipment() {
        String address = etAddress.getText().toString().trim();
        String date = etDate.getText().toString().trim();
        String time = etTime.getText().toString().trim();
        String type = etType.getText().toString().trim();

        if (address.isEmpty() || date.isEmpty() || time.isEmpty() || type.isEmpty()) {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_LONG).show();
            return;
        }

        if (!locationSelected) {
            Toast.makeText(this, "Seleccione una ubicación para el envío", Toast.LENGTH_LONG).show();
            return;
        }

        boolean inserted = dbHelper.insertShipment(userEmail, address, date, time, type,
            "Pendiente", selectedLat, selectedLon);

        if (inserted) {
            Toast.makeText(this, "Envío guardado correctamente", Toast.LENGTH_LONG).show();
            etAddress.setText("");
            etDate.setText("");
            etTime.setText("");
            etType.setText("");
            locationSelected = false;
            selectedLat = 0.0;
            selectedLon = 0.0;
            selectedLocation = null;
            mapPreviewCard.setVisibility(View.GONE);
            insertNotificationForUser(address);
        } else {
            Toast.makeText(this, "Error al guardar el envío", Toast.LENGTH_LONG).show();
        }
    }

    private void insertNotificationForUser(String address) {
        long now = System.currentTimeMillis();
        dbHelper.insertNotification(
            userEmail,
            getString(R.string.notification_title_new_shipment),
            getString(R.string.notification_body_new_shipment, address),
            now
        );
    }

    private void openMapDialog() {
        new MapDialog(this, (address, latitude, longitude) -> {
            etAddress.setText(address);
            selectedLocation = new GeoPoint(latitude, longitude);
            selectedLat = latitude;
            selectedLon = longitude;
            locationSelected = true;
            updateMapPreview();
        }).show();
    }

    private void updateMapPreview() {
        if (selectedLocation == null) {
            return;
        }
        mapPreviewCard.setVisibility(View.VISIBLE);
        mapPreview.getController().setCenter(selectedLocation);
        mapPreview.getOverlays().clear();
        Marker marker = new Marker(mapPreview);
        marker.setPosition(selectedLocation);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mapPreview.getOverlays().add(marker);
        mapPreview.invalidate();
    }

    private void ensureLocationPermission(Runnable onGranted) {
        if (hasLocationPermission()) {
            onGranted.run();
            return;
        }
        pendingLocationPermissionAction = onGranted;
        ActivityCompat.requestPermissions(this,
            new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
            LOCATION_PERMISSION_REQUEST);
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED;
    }

    @SuppressLint("MissingPermission")
    private void fetchCurrentLocation() {
        if (!hasLocationPermission()) {
            return;
        }
        setLocationFetchInProgress(true);

        fusedLocationClient.getLastLocation()
            .addOnSuccessListener(this, location -> {
                if (location != null) {
                    onLocationObtained(location);
                } else {
                    requestSingleLocationUpdate();
                }
            })
            .addOnFailureListener(e -> {
                setLocationFetchInProgress(false);
                Toast.makeText(this, R.string.location_error_generic, Toast.LENGTH_SHORT).show();
            });
    }

    @SuppressLint("MissingPermission")
    private void requestSingleLocationUpdate() {
        if (!hasLocationPermission()) {
            setLocationFetchInProgress(false);
            return;
        }
        LocationRequest locationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(2000)
            .setFastestInterval(1000)
            .setNumUpdates(1);
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback,
            Looper.getMainLooper());
    }

    private void onLocationObtained(@NonNull Location location) {
        selectedLat = location.getLatitude();
        selectedLon = location.getLongitude();
        selectedLocation = new GeoPoint(selectedLat, selectedLon);
        locationSelected = true;

        updateMapPreview();

        String fallbackAddress = getString(R.string.location_fallback_address,
            selectedLat, selectedLon);
        etAddress.setText(fallbackAddress);
        Toast.makeText(this, R.string.location_ready, Toast.LENGTH_SHORT).show();

        resolveAddressAsync(location);
        setLocationFetchInProgress(false);
    }

    private void resolveAddressAsync(@NonNull Location location) {
        geocodeExecutor.execute(() -> {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            String addressText = getString(R.string.location_fallback_address,
                location.getLatitude(), location.getLongitude());

            try {
                List<Address> addresses = geocoder.getFromLocation(
                    location.getLatitude(), location.getLongitude(), 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    StringBuilder builder = new StringBuilder();
                    for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                        builder.append(address.getAddressLine(i));
                        if (i < address.getMaxAddressLineIndex()) {
                            builder.append(", ");
                        }
                    }
                    addressText = builder.toString();
                }
            } catch (IOException ignored) {
                // Si falla la geocodificacion usamos el fallback.
            }

            final String resolvedAddress = addressText;
            runOnUiThread(() -> {
                if (!isFinishing()) {
                    etAddress.setText(resolvedAddress);
                }
            });
        });
    }

    private void setLocationFetchInProgress(boolean inProgress) {
        btnUseCurrentLocation.setEnabled(!inProgress);
        btnUseCurrentLocation.setText(inProgress
            ? getString(R.string.location_fetching)
            : getString(R.string.use_current_location));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != LOCATION_PERMISSION_REQUEST) {
            return;
        }

        boolean granted = grantResults.length > 0
            && grantResults[0] == PackageManager.PERMISSION_GRANTED;

        if (granted && pendingLocationPermissionAction != null) {
            pendingLocationPermissionAction.run();
        } else if (!granted) {
            Toast.makeText(this, R.string.location_permission_denied, Toast.LENGTH_SHORT).show();
        }

        pendingLocationPermissionAction = null;
    }
}
