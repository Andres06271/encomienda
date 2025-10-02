package co.edu.unipiloto.encomienda.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import co.edu.unipiloto.encomienda.R;
import co.edu.unipiloto.encomienda.db.DBHelper;

public class MenuActivity extends AppCompatActivity {

    private Button btnRegistrarPedido, btnVerPedidos, btnGestionarEnvios, btnPedidosAsignados;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        btnRegistrarPedido = findViewById(R.id.btnRegistrarPedido);
        btnVerPedidos = findViewById(R.id.btnVerPedidos);
        btnGestionarEnvios = findViewById(R.id.btnGestionarEnvios);
        btnPedidosAsignados = findViewById(R.id.btnPedidosAsignados);

        // Recibir email del usuario desde el login
        String userEmail = getIntent().getStringExtra("userEmail");
        dbHelper = new DBHelper(this);
        String role = dbHelper.getUserRole(userEmail);

        // Mostrar/ocultar botones según el rol
        switch (role) {
            case "admin":
                btnRegistrarPedido.setVisibility(View.GONE);
                btnVerPedidos.setVisibility(View.GONE);
                btnPedidosAsignados.setVisibility(View.GONE);
                btnGestionarEnvios.setVisibility(View.VISIBLE);
                break;
            case "courier":
                btnRegistrarPedido.setVisibility(View.GONE);
                btnVerPedidos.setVisibility(View.GONE);
                btnPedidosAsignados.setVisibility(View.VISIBLE);
                btnGestionarEnvios.setVisibility(View.GONE);
                break;
            default: // usuario normal
                btnRegistrarPedido.setVisibility(View.VISIBLE);
                btnVerPedidos.setVisibility(View.VISIBLE);
                btnPedidosAsignados.setVisibility(View.GONE);
                btnGestionarEnvios.setVisibility(View.GONE);
                break;
        }

        // Ir a registrar envío
        btnRegistrarPedido.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, ShipmentActivity.class);
            intent.putExtra("userEmail", userEmail);
            startActivity(intent);
        });

        // Ir a lista de envíos
        btnVerPedidos.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, ShipmentListActivity.class);
            intent.putExtra("userEmail", userEmail);
            startActivity(intent);
        });

        // Ir a gestionar envíos (solo para admin)
        btnGestionarEnvios.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, AdminShipmentListActivity.class);
            startActivity(intent);
        });

        // Ir a pedidos asignados (solo para courier)
        btnPedidosAsignados.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, CourierShipmentListActivity.class);
            intent.putExtra("userEmail", userEmail);
            startActivity(intent);
        });
    }
}
