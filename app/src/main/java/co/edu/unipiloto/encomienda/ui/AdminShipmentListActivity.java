package co.edu.unipiloto.encomienda.ui;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import co.edu.unipiloto.encomienda.R;
import co.edu.unipiloto.encomienda.db.DBHelper;
import co.edu.unipiloto.encomienda.model.Shipment;
import co.edu.unipiloto.encomienda.sync.BackendSync;

public class AdminShipmentListActivity extends AppCompatActivity {
    
    private RecyclerView recyclerView;
    private ShipmentAdapter adapter;
    private List<Shipment> shipmentList;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipment_list);

        recyclerView = findViewById(R.id.recyclerShipments);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        dbHelper = new DBHelper(this);
        shipmentList = new ArrayList<>();
        
        loadAllShipments();
    }

    private void loadAllShipments() {
        shipmentList.clear();
        
        try (Cursor cursor = dbHelper.getAllShipments()) {
            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                    String userEmail = cursor.getString(cursor.getColumnIndexOrThrow("userEmail"));
                    String address = cursor.getString(cursor.getColumnIndexOrThrow("address"));
                    String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                    String time = cursor.getString(cursor.getColumnIndexOrThrow("time"));
                    String type = cursor.getString(cursor.getColumnIndexOrThrow("type"));
                    String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));

                    Shipment shipment = new Shipment(id, address, date, time, type, status);
                    shipmentList.add(shipment);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error al cargar envíos: " + e.getMessage(), 
                Toast.LENGTH_LONG).show();
        }

        adapter = new ShipmentAdapter(shipmentList);
        adapter.setOnItemClickListener(shipment -> showAssignDialog(shipment));
        recyclerView.setAdapter(adapter);
    }

    private void showAssignDialog(Shipment shipment) {
        View view = getLayoutInflater().inflate(R.layout.dialog_select_courier, null);
        Spinner spinnerCouriers = view.findViewById(R.id.spinnerCouriers);
        
        List<String> couriers = dbHelper.getAllCouriers();
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, couriers);
        spinnerAdapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item);
        spinnerCouriers.setAdapter(spinnerAdapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view)
               .setPositiveButton("Asignar", (dialog, which) -> {
                    String selectedCourier = spinnerCouriers.getSelectedItem().toString();
                    assignShipment(shipment.getId(), selectedCourier);
               })
               .setNegativeButton("Cancelar", null)
               .show();
    }

    private void assignShipment(int shipmentId, String courierEmail) {
        if (dbHelper.assignShipmentToCourier(shipmentId, courierEmail)) {
            Toast.makeText(this, "Envío asignado correctamente", 
                Toast.LENGTH_SHORT).show();
            // Best-effort: crear en backend y enlazar remoteId
            BackendSync.pushShipmentCreateAndBind(this, dbHelper, shipmentId);
            loadAllShipments(); // Recargar la lista
        } else {
            Toast.makeText(this, "Error al asignar el envío", 
                Toast.LENGTH_SHORT).show();
        }
    }
}
