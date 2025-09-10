package co.edu.unipiloto.encomienda.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import co.edu.unipiloto.encomienda.R;

public class MenuActivity extends AppCompatActivity {

    private Button btnRegistrarPedido, btnVerPedidos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        btnRegistrarPedido = findViewById(R.id.btnRegistrarPedido);
        btnVerPedidos = findViewById(R.id.btnVerPedidos);

        // Recibir email del usuario desde el login
        String userEmail = getIntent().getStringExtra("userEmail");

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
    }
}
