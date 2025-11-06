package co.edu.unipiloto.encomienda.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import co.edu.unipiloto.encomienda.R;
import co.edu.unipiloto.encomienda.db.DBHelper;

public class ShipmentActivity extends AppCompatActivity {

    private EditText etAddress, etDate, etTime, etType;
    private Button btnSaveShipment;
    private DBHelper dbHelper;
    private String userEmail;
    private static final int LOCATION_PERMISSION_REQUEST = 1001;
    private MapView mapPreview;
    private CardView mapPreviewCard;
    private GeoPoint selectedLocation;

    // Nuevos campos para lat/lon seleccionadas
    private double selectedLat = 0.0;
    private double selectedLon = 0.0;
    private boolean locationSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipment);

        dbHelper = new DBHelper(this);

        etAddress = findViewById(R.id.etAddress);
        etDate = findViewById(R.id.etDate);
        etTime = findViewById(R.id.etTime);
        etType = findViewById(R.id.etType);
        btnSaveShipment = findViewById(R.id.btnSaveShipment);

        userEmail = getIntent().getStringExtra("userEmail");

        etAddress.setOnClickListener(v -> showMapDialog());
        btnSaveShipment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveShipment();
            }
        });

        mapPreview = findViewById(R.id.mapPreview);
        mapPreviewCard = findViewById(R.id.mapPreviewCard);

        Configuration.getInstance().load(this, 
            PreferenceManager.getDefaultSharedPreferences(this));

        // Configurar mapa preview
        mapPreview.setMultiTouchControls(true);
        mapPreview.getController().setZoom(15.0);
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

        // Requerimos que el usuario haya seleccionado una ubicación desde el MapDialog
        if (!locationSelected) {
            Toast.makeText(this, "Seleccione una ubicación para el envío", Toast.LENGTH_LONG).show();
            return;
        }

        // Llamada corregida incluyendo el status "Pendiente"
        boolean inserted = dbHelper.insertShipment(userEmail, address, date, time, type, "Pendiente", selectedLat, selectedLon);

        if (inserted) {
            Toast.makeText(this, "Envío guardado correctamente", Toast.LENGTH_LONG).show();
            etAddress.setText("");
            etDate.setText("");
            etTime.setText("");
            etType.setText("");
            // reset ubicación seleccionada
            locationSelected = false;
            selectedLat = 0.0;
            selectedLon = 0.0;
            mapPreviewCard.setVisibility(View.GONE);
        } else {
            Toast.makeText(this, "Error al guardar el envío", Toast.LENGTH_LONG).show();
        }
    }

    private void showMapDialog() {
        if (checkLocationPermission()) {
            new MapDialog(this, (address, latitude, longitude) -> {
                etAddress.setText(address);
                selectedLocation = new GeoPoint(latitude, longitude);
                // Guardar lat/lon seleccionadas
                selectedLat = latitude;
                selectedLon = longitude;
                locationSelected = true;
                updateMapPreview();
            }).show();
        }
    }

    private void updateMapPreview() {
        if (selectedLocation != null) {
            mapPreviewCard.setVisibility(View.VISIBLE);
            mapPreview.getController().setCenter(selectedLocation);

            // Añadir marcador
            mapPreview.getOverlays().clear();
            Marker marker = new Marker(mapPreview);
            marker.setPosition(selectedLocation);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            mapPreview.getOverlays().add(marker);

            mapPreview.invalidate();
        }
    }

    private boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST);
            return false;
        }
        return true;
    }
}
