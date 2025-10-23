package co.edu.unipiloto.encomienda.ui;

import android.app.Dialog;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import co.edu.unipiloto.encomienda.R;

public class MapDialog extends Dialog {
    private final Context context;
    private MapView mapView;
    private EditText searchEditText;
    private Marker marker;
    private OnLocationSelectedListener listener;
    private GeoPoint selectedLocation;

    public interface OnLocationSelectedListener {
        void onLocationSelected(String address, double latitude, double longitude);
    }

    public MapDialog(Context context, OnLocationSelectedListener listener) {
        super(context);
        this.context = context;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_map);

        Configuration.getInstance().load(context, 
            PreferenceManager.getDefaultSharedPreferences(context));

        mapView = findViewById(R.id.mapView);
        searchEditText = findViewById(R.id.etSearchLocation);
        
        setupMap();
        setupSearchBar();
        setupButtons();
    }

    private void setupMap() {
        mapView.setMultiTouchControls(true);
        
        // Centrar en Bogotá por defecto
        GeoPoint startPoint = new GeoPoint(4.6097, -74.0817);
        mapView.getController().setCenter(startPoint);
        mapView.getController().setZoom(12.0);

        marker = new Marker(mapView);
        mapView.getOverlays().add(marker);

        mapView.setOnTouchListener((v, event) -> {
            selectedLocation = (GeoPoint) mapView.getProjection()
                .fromPixels((int)event.getX(), (int)event.getY());
            updateMarkerPosition(selectedLocation);
            updateSearchBarText(selectedLocation);
            return false;
        });
    }

    private void setupSearchBar() {
        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchLocation(searchEditText.getText().toString());
                return true;
            }
            return false;
        });
    }

    private void setupButtons() {
        findViewById(R.id.btnConfirmLocation).setOnClickListener(v -> {
            if (selectedLocation != null) {
                String address = getAddressFromLocation(
                    selectedLocation.getLatitude(), 
                    selectedLocation.getLongitude()
                );
                listener.onLocationSelected(
                    address, 
                    selectedLocation.getLatitude(), 
                    selectedLocation.getLongitude()
                );
                dismiss();
            }
        });

        findViewById(R.id.btnCancel).setOnClickListener(v -> dismiss());
    }

    private void searchLocation(String query) {
        try {
            Geocoder geocoder = new Geocoder(context);
            List<Address> addresses = geocoder.getFromLocationName(query, 1);
            if (!addresses.isEmpty()) {
                Address address = addresses.get(0);
                GeoPoint point = new GeoPoint(address.getLatitude(), address.getLongitude());
                selectedLocation = point;
                mapView.getController().animateTo(point);
                updateMarkerPosition(point);
            }
        } catch (IOException e) {
            Toast.makeText(context, "Error buscando ubicación", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateMarkerPosition(GeoPoint point) {
        marker.setPosition(point);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mapView.invalidate();
    }

    private void updateSearchBarText(GeoPoint point) {
        String address = getAddressFromLocation(point.getLatitude(), point.getLongitude());
        searchEditText.setText(address);
    }

    private String getAddressFromLocation(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (!addresses.isEmpty()) {
                Address address = addresses.get(0);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    sb.append(address.getAddressLine(i));
                    if (i < address.getMaxAddressLineIndex()) sb.append(", ");
                }
                return sb.toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return latitude + ", " + longitude;
    }
}
