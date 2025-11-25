package co.edu.unipiloto.encomienda.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import co.edu.unipiloto.encomienda.R;
import co.edu.unipiloto.encomienda.api.dto.RemoteShipment;
import co.edu.unipiloto.encomienda.model.Shipment;
import co.edu.unipiloto.encomienda.repository.ShipmentRepository;

public class CourierShipmentListActivity extends AppCompatActivity {
    
    private RecyclerView recyclerView;
    private ShipmentAdapter adapter;
    private List<Shipment> shipmentList;
    private ShipmentRepository shipmentRepository;
    private ProgressBar progressBar;
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

        shipmentRepository = new ShipmentRepository();
        shipmentList = new ArrayList<>();
        
        loadAssignedShipments();
    }

    private void loadAssignedShipments() {
        shipmentList.clear();
        progressBar.setVisibility(View.VISIBLE);
        
        // Consumir API del backend para obtener shipments del courier
        shipmentRepository.getShipmentsByCourier(courierEmail, new ShipmentRepository.ShipmentListCallback() {
            @Override
            public void onSuccess(List<RemoteShipment> remoteShipments) {
                progressBar.setVisibility(View.GONE);
                shipmentList.clear();
                
                for (RemoteShipment remote : remoteShipments) {
                    Shipment local = new Shipment(
                            remote.getId().intValue(),
                            remote.getAddress(),
                            remote.getCreatedAt() != null ? remote.getCreatedAt().toString() : "",
                            "",
                            remote.getType(),
                            remote.getStatus()
                    );
                    shipmentList.add(local);
                }

                if (adapter == null) {
                    adapter = new ShipmentAdapter(shipmentList);
                    adapter.setOnItemClickListener(shipment -> showChangeStatusDialog(shipment));
                    recyclerView.setAdapter(adapter);
                } else {
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onError(String message) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(CourierShipmentListActivity.this, 
                        "Error al cargar envíos: " + message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showChangeStatusDialog(Shipment shipment) {
        String[] estados = {"En camino", "Entregado"};
        int selectedIdx = getEstadoIndex(shipment.getStatus());
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Cambiar estado")
            .setSingleChoiceItems(estados, selectedIdx, null)
            .setPositiveButton("Guardar", (dialog, which) -> {
                androidx.appcompat.app.AlertDialog alert = (androidx.appcompat.app.AlertDialog) dialog;
                int selectedPosition = alert.getListView().getCheckedItemPosition();
                String nuevoEstado = estados[selectedPosition];
                
                // Actualizar estado en el backend
                progressBar.setVisibility(View.VISIBLE);
                shipmentRepository.updateShipmentStatus(shipment.getId(), nuevoEstado, 
                    new ShipmentRepository.ShipmentCallback() {
                        @Override
                        public void onSuccess(RemoteShipment updatedShipment) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(CourierShipmentListActivity.this, 
                                    "Estado actualizado", Toast.LENGTH_SHORT).show();
                            loadAssignedShipments();
                        }

                        @Override
                        public void onError(String message) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(CourierShipmentListActivity.this, 
                                    "Error al actualizar estado: " + message, Toast.LENGTH_SHORT).show();
                        }
                    });
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }

    private int getEstadoIndex(String estado) {
        if ("En camino".equals(estado)) return 0;
        if ("Entregado".equals(estado)) return 1;
        return 0;
    }
}
