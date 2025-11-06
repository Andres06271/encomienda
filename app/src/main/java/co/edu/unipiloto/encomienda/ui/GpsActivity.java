package co.edu.unipiloto.encomienda.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import co.edu.unipiloto.encomienda.R;

public class GpsActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST = 3001;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private final ExecutorService geocodeExecutor = Executors.newSingleThreadExecutor();

    private TextView tvLocationStatus;
    private TextView tvLocationCoords;
    private TextView tvLocationAddress;
    private ProgressBar progressBar;
    private Button btnFetchLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        tvLocationStatus = findViewById(R.id.tvLocationStatus);
        tvLocationCoords = findViewById(R.id.tvLocationCoords);
        tvLocationAddress = findViewById(R.id.tvLocationAddress);
        progressBar = findViewById(R.id.progressLocation);
        btnFetchLocation = findViewById(R.id.btnFetchLocation);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                progressBar.setVisibility(View.GONE);
                if (locationResult.getLastLocation() != null) {
                    updateLocationUi(locationResult.getLastLocation());
                } else {
                    tvLocationStatus.setText(R.string.location_not_found);
                }
                fusedLocationClient.removeLocationUpdates(this);
            }
        };

        btnFetchLocation.setOnClickListener(v -> {
            if (hasLocationPermission()) {
                fetchCurrentLocation();
            } else {
                requestLocationPermission();
            }
        });

        if (hasLocationPermission()) {
            fetchCurrentLocation();
        } else {
            requestLocationPermission();
        }
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
            LOCATION_PERMISSION_REQUEST);
    }

    @SuppressLint("MissingPermission")
    private void fetchCurrentLocation() {
        if (!hasLocationPermission()) {
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        tvLocationStatus.setText(R.string.location_fetching);

        fusedLocationClient.getLastLocation()
            .addOnSuccessListener(this, location -> {
                if (location != null) {
                    progressBar.setVisibility(View.GONE);
                    updateLocationUi(location);
                } else {
                    requestSingleLocationUpdate();
                }
            })
            .addOnFailureListener(e -> {
                progressBar.setVisibility(View.GONE);
                tvLocationStatus.setText(R.string.location_error_generic);
                Toast.makeText(this, R.string.location_error_generic, Toast.LENGTH_SHORT).show();
            });
    }

    @SuppressLint("MissingPermission")
    private void requestSingleLocationUpdate() {
        if (!hasLocationPermission()) {
            return;
        }
        LocationRequest locationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(2000)
            .setFastestInterval(1000)
            .setNumUpdates(1);
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void updateLocationUi(@NonNull Location location) {
        String coords = getString(R.string.location_coords_pattern,
            location.getLatitude(), location.getLongitude());
        tvLocationCoords.setText(coords);
        tvLocationStatus.setText(R.string.location_ready);
        tvLocationAddress.setText(getString(R.string.location_address_loading));
        resolveAddressAsync(location);
    }

    private void resolveAddressAsync(@NonNull Location location) {
        geocodeExecutor.execute(() -> {
            String addressText = getString(R.string.location_address_unavailable);
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
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
                // GeoCoder can fail due to network issues; we fall back to coordinates.
            }

            final String finalAddress = addressText;
            runOnUiThread(() -> tvLocationAddress.setText(
                getString(R.string.location_address_pattern, finalAddress)));
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                              @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchCurrentLocation();
            } else {
                tvLocationStatus.setText(R.string.location_permission_denied);
                Toast.makeText(this, R.string.location_permission_denied, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
        geocodeExecutor.shutdownNow();
        super.onDestroy();
    }
}
