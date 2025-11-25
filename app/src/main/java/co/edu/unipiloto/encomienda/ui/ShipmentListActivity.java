package co.edu.unipiloto.encomienda.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import co.edu.unipiloto.encomienda.R;
import co.edu.unipiloto.encomienda.api.dto.RemoteShipment;
import co.edu.unipiloto.encomienda.model.Shipment;
import co.edu.unipiloto.encomienda.repository.ShipmentRepository;

public class ShipmentListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ShipmentAdapter adapter;
    private List<Shipment> shipmentList;
    private ShipmentRepository shipmentRepository;
    private ProgressBar progressBar;
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

        if (progressBar == null) {
            // Si no existe progressBar en el layout, creamos una referencia dummy
            progressBar = new ProgressBar(this);
            progressBar.setVisibility(View.GONE);
        }

        shipmentRepository = new ShipmentRepository();
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
        progressBar.setVisibility(View.VISIBLE);

        // Consumir API del backend para obtener shipments del usuario
        shipmentRepository.getShipmentsByUser(userEmail, new ShipmentRepository.ShipmentListCallback() {
            @Override
            public void onSuccess(List<RemoteShipment> remoteShipments) {
                progressBar.setVisibility(View.GONE);
                shipmentList.clear();
                
                // Convertir RemoteShipment a Shipment local para el adapter
                for (RemoteShipment remote : remoteShipments) {
                    Shipment local = new Shipment(
                            remote.getId().intValue(),
                            remote.getAddress(),
                            remote.getCreatedAt() != null ? remote.getCreatedAt().toString() : "",
                            "", // time no se usa en backend
                            remote.getType(),
                            remote.getStatus()
                    );
                    shipmentList.add(local);
                }

                if (adapter == null) {
                    adapter = new ShipmentAdapter(shipmentList);
                    recyclerView.setAdapter(adapter);
                } else {
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onError(String message) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ShipmentListActivity.this, 
                        "Error al cargar envíos: " + message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadShipmentsByStatus(String status) {
        shipmentList.clear();
        progressBar.setVisibility(View.VISIBLE);

        // Consumir API del backend para filtrar por estado
        shipmentRepository.getShipmentsByStatus(status, new ShipmentRepository.ShipmentListCallback() {
            @Override
            public void onSuccess(List<RemoteShipment> remoteShipments) {
                progressBar.setVisibility(View.GONE);
                shipmentList.clear();
                
                // Filtrar solo los del usuario actual
                for (RemoteShipment remote : remoteShipments) {
                    if (remote.getUserEmail() != null && remote.getUserEmail().equals(userEmail)) {
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
                }

                if (adapter == null) {
                    adapter = new ShipmentAdapter(shipmentList);
                    recyclerView.setAdapter(adapter);
                } else {
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onError(String message) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ShipmentListActivity.this, 
                        "Error al cargar envíos: " + message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
