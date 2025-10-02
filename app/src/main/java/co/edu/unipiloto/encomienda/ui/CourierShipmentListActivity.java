package co.edu.unipiloto.encomienda.ui;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import co.edu.unipiloto.encomienda.R;
import co.edu.unipiloto.encomienda.model.Shipment;
import co.edu.unipiloto.encomienda.db.DBHelper;

public class CourierShipmentListActivity extends AppCompatActivity {
    
    private RecyclerView recyclerView;
    private ShipmentAdapter adapter;
    private List<Shipment> shipmentList;
    private DBHelper dbHelper;
    private String courierEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courier_shipments);

        courierEmail = getIntent().getStringExtra("userEmail");
        if (courierEmail == null) {
            Toast.makeText(this, "Error: Mensajero no identificado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        recyclerView = findViewById(R.id.recyclerShipments);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        dbHelper = new DBHelper(this);
        shipmentList = new ArrayList<>();
        
        loadAssignedShipments();
    }

    private void loadAssignedShipments() {
        shipmentList.clear();
        
        try (Cursor cursor = dbHelper.getShipmentsAssignedToCourier(courierEmail)) {
            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                    String address = cursor.getString(cursor.getColumnIndexOrThrow("address"));
                    String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                    String time = cursor.getString(cursor.getColumnIndexOrThrow("time"));
                    String type = cursor.getString(cursor.getColumnIndexOrThrow("type"));
                    String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));

                    shipmentList.add(new Shipment(id, address, date, time, type, status));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error al cargar envíos: " + e.getMessage(), 
                Toast.LENGTH_LONG).show();
        }

        adapter = new ShipmentAdapter(shipmentList);
        recyclerView.setAdapter(adapter);
    }
}
