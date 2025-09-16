package co.edu.unipiloto.encomienda.ui;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import co.edu.unipiloto.encomienda.R;
import co.edu.unipiloto.encomienda.db.DBHelper;
import co.edu.unipiloto.encomienda.model.Shipment;

public class ShipmentListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ShipmentAdapter adapter;
    private List<Shipment> shipmentList;
    private DBHelper dbHelper;
    private String userEmail; // email del usuario logueado

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipment_list);

        // Obtener email del usuario desde intent
        userEmail = getIntent().getStringExtra("userEmail");
        if (userEmail == null) {
            Toast.makeText(this, "Error: Usuario no encontrado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        recyclerView = findViewById(R.id.recyclerShipments);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        dbHelper = new DBHelper(this);
        shipmentList = new ArrayList<>();

        // Configurar el spinner de filtro
        Spinner spinnerFilter = findViewById(R.id.spinnerFilterType);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Todos", "Pendiente", "En camino", "Entregado"}
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(spinnerAdapter);

        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                if (selected.equals("Todos")) {
                    loadShipments(); // 🔹 carga todo
                } else {
                    loadShipmentsByStatus(selected); // 🔹 carga filtrado
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Cargar por defecto todos los envíos
        loadShipments();
    }

    private void loadShipments() {
        shipmentList.clear();

        // Consultar envíos del usuario (ahora también con status)
        try (Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                "SELECT id, address, date, time, type, status FROM shipments WHERE userEmail = ?",
                new String[]{userEmail})) {

            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(0);
                    String address = cursor.getString(1);
                    String date = cursor.getString(2);
                    String time = cursor.getString(3);
                    String type = cursor.getString(4);
                    String status = cursor.getString(5);

                    shipmentList.add(new Shipment(id, address, date, time, type, status));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error al cargar envíos: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        adapter = new ShipmentAdapter(shipmentList);
        recyclerView.setAdapter(adapter);
    }

    private void loadShipmentsByStatus(String status) {
        shipmentList.clear();

        try (Cursor cursor = dbHelper.getShipmentsByStatus(userEmail, status)) {
            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                    String address = cursor.getString(cursor.getColumnIndexOrThrow("address"));
                    String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                    String time = cursor.getString(cursor.getColumnIndexOrThrow("time"));
                    String type = cursor.getString(cursor.getColumnIndexOrThrow("type"));
                    String shipmentStatus = cursor.getString(cursor.getColumnIndexOrThrow("status"));

                    shipmentList.add(new Shipment(id, address, date, time, type, shipmentStatus));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error al cargar envíos: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        adapter = new ShipmentAdapter(shipmentList);
        recyclerView.setAdapter(adapter);
    }
}
