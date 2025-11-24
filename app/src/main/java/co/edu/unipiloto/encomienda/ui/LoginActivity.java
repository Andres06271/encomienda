package co.edu.unipiloto.encomienda.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import co.edu.unipiloto.encomienda.R;
import co.edu.unipiloto.encomienda.db.DBHelper;
import co.edu.unipiloto.encomienda.utils.NotificationHelper;

public class LoginActivity extends AppCompatActivity {

    private EditText edtEmail, edtPassword;
    private Button btnLogin;
    private TextView btnGoRegister; // antes Button, ahora TextView
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

            // Crear canal de notificaciones al iniciar la app
            NotificationHelper.createNotificationChannel(this);

        // Vincular vistas con IDs del layout
        edtEmail = findViewById(R.id.etEmailLogin);
        edtPassword = findViewById(R.id.etPasswordLogin);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoRegister = findViewById(R.id.tvGoRegister);

        dbHelper = new DBHelper(this);

        // Listener botón login
        btnLogin.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim(); // Password en texto plano

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Complete los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean valid = dbHelper.validateUser(email, password); // Usar password en texto plano
            if (valid) {
                Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
                intent.putExtra("userEmail", email);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Credenciales inválidas", Toast.LENGTH_SHORT).show();
            }
        });

        // Listener enlace registro
        btnGoRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }
}

