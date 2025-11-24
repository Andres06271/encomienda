package co.edu.unipiloto.encomienda.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import co.edu.unipiloto.encomienda.R;
import co.edu.unipiloto.encomienda.db.DBHelper;
import co.edu.unipiloto.encomienda.db.User;

public class AdminUserListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_Encomienda); // Forzar modo oscuro si el sistema lo tiene
        setContentView(R.layout.activity_user_list);

        recyclerView = findViewById(R.id.recyclerUsers);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        dbHelper = new DBHelper(this);
        loadUsers();
    }

    private void loadUsers() {
        List<User> userList = dbHelper.getAllUsers();
        adapter = new UserAdapter(userList);
        adapter.setOnRoleEditListener(user -> showRoleEditDialog(user));
        recyclerView.setAdapter(adapter);
    }

    private void showRoleEditDialog(User user) {
        View view = getLayoutInflater().inflate(R.layout.dialog_edit_role, null);
        android.widget.Spinner spinnerRole = view.findViewById(R.id.spinnerRole);
        String[] roles = {"user", "admin", "courier"};
        android.widget.ArrayAdapter<String> spinnerAdapter = new android.widget.ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, roles);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(spinnerAdapter);
        int pos = java.util.Arrays.asList(roles).indexOf(user.role);
        spinnerRole.setSelection(pos >= 0 ? pos : 0);

        new AlertDialog.Builder(this)
            .setTitle("Editar rol")
            .setView(view)
            .setPositiveButton("Guardar", (dialog, which) -> {
                String newRole = spinnerRole.getSelectedItem().toString();
                if (dbHelper.updateUserRole(user.id, newRole)) {
                    Toast.makeText(this, "Rol actualizado", Toast.LENGTH_SHORT).show();
                    loadUsers();
                } else {
                    Toast.makeText(this, "Error al actualizar rol", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }
}
