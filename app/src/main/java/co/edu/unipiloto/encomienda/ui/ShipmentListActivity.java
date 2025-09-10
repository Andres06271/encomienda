package co.edu.unipiloto.encomienda.ui;

import android.os.Bundle;
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

        loadShipments();
    }

    private void loadShipments() {
        shipmentList.clear();

        // Consultar envíos del usuario
        try (android.database.Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                "SELECT id, address, date, time, type FROM shipments WHERE userEmail = ?",
                new String[]{userEmail})) {

            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(0);
                    String address = cursor.getString(1);
                    String date = cursor.getString(2);
                    String time = cursor.getString(3);
                    String type = cursor.getString(4);

                    // Usamos el constructor con ID
                    Shipment shipment = new Shipment(id, address, date, time, type);
                    shipmentList.add(shipment);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error al cargar envíos: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        adapter = new ShipmentAdapter(shipmentList);
        recyclerView.setAdapter(adapter);
    }
}
