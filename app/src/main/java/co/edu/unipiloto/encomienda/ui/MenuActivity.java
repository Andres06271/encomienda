package co.edu.unipiloto.encomienda.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import co.edu.unipiloto.encomienda.R;
import co.edu.unipiloto.encomienda.db.DBHelper;

public class MenuActivity extends AppCompatActivity {

    private Button btnRegistrarPedido, btnVerPedidos, btnGestionarEnvios, btnPedidosAsignados, btnNotifications, btnAdminUsuarios;
    private TextView tvNotificationBadge;
    private DBHelper dbHelper;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        btnRegistrarPedido = findViewById(R.id.btnRegistrarPedido);
        btnVerPedidos = findViewById(R.id.btnVerPedidos);
        btnGestionarEnvios = findViewById(R.id.btnGestionarEnvios);
        btnPedidosAsignados = findViewById(R.id.btnPedidosAsignados);
    btnNotifications = findViewById(R.id.btnNotifications);
    btnAdminUsuarios = findViewById(R.id.btnAdminUsuarios);
    tvNotificationBadge = findViewById(R.id.tvNotificationBadge);

        // Recibir email del usuario desde el login
        userEmail = getIntent().getStringExtra("userEmail");
        dbHelper = new DBHelper(this);
        String role = dbHelper.getUserRole(userEmail);

        // Mostrar/ocultar botones según el rol
        switch (role) {
            case "admin":
                btnRegistrarPedido.setVisibility(View.GONE);
                btnVerPedidos.setVisibility(View.GONE);
                btnPedidosAsignados.setVisibility(View.GONE);
                btnGestionarEnvios.setVisibility(View.VISIBLE);
                btnAdminUsuarios.setVisibility(View.VISIBLE);
                break;
            case "courier":
                btnRegistrarPedido.setVisibility(View.GONE);
                btnVerPedidos.setVisibility(View.GONE);
                btnPedidosAsignados.setVisibility(View.VISIBLE);
                btnGestionarEnvios.setVisibility(View.GONE);
                btnAdminUsuarios.setVisibility(View.GONE);
                break;
            default: // usuario normal
                btnRegistrarPedido.setVisibility(View.VISIBLE);
                btnVerPedidos.setVisibility(View.VISIBLE);
                btnPedidosAsignados.setVisibility(View.GONE);
                btnGestionarEnvios.setVisibility(View.GONE);
                btnAdminUsuarios.setVisibility(View.GONE);
                break;
        }
        // Ir a administrar usuarios (solo admin)
        btnAdminUsuarios.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, AdminUserListActivity.class);
            startActivity(intent);
        });

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
            Intent intent = new Intent(MenuActivity.this, CourierMapActivity.class);
            intent.putExtra("userEmail", userEmail);
            startActivity(intent);
        });

        // Ir a notificaciones (disponible para todos)
        btnNotifications.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, NotificationCenterActivity.class);
            intent.putExtra("userEmail", userEmail);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateNotificationBadge();
    }

    private void updateNotificationBadge() {
        if (userEmail == null || dbHelper == null) {
            return;
        }
        int unreadCount = dbHelper.countUnreadNotifications(userEmail);
        if (unreadCount > 0) {
            tvNotificationBadge.setVisibility(View.VISIBLE);
            tvNotificationBadge.setText(String.valueOf(unreadCount));
        } else {
            tvNotificationBadge.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}
