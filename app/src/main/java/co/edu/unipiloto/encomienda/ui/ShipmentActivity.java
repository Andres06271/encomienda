package co.edu.unipiloto.encomienda.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import co.edu.unipiloto.encomienda.R;
import co.edu.unipiloto.encomienda.db.DBHelper;

public class ShipmentActivity extends AppCompatActivity {

    private EditText etAddress, etDate, etTime, etType;
    private Button btnSaveShipment;
    private DBHelper dbHelper;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipment);

        dbHelper = new DBHelper(this);

        etAddress = findViewById(R.id.etAddress);
        etDate = findViewById(R.id.etDate);
        etTime = findViewById(R.id.etTime);
        etType = findViewById(R.id.etType);
        btnSaveShipment = findViewById(R.id.btnSaveShipment);

        userEmail = getIntent().getStringExtra("userEmail");

        btnSaveShipment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveShipment();
            }
        });
    }

    private void saveShipment() {
        String address = etAddress.getText().toString().trim();
        String date = etDate.getText().toString().trim();
        String time = etTime.getText().toString().trim();
        String type = etType.getText().toString().trim();

        if (address.isEmpty() || date.isEmpty() || time.isEmpty() || type.isEmpty()) {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_LONG).show();
            return;
        }

        boolean inserted = dbHelper.insertShipment(userEmail, address, date, time, type);

        if (inserted) {
            Toast.makeText(this, "Envío guardado correctamente", Toast.LENGTH_LONG).show();
            etAddress.setText("");
            etDate.setText("");
            etTime.setText("");
            etType.setText("");
        } else {
            Toast.makeText(this, "Error al guardar el envío", Toast.LENGTH_LONG).show();
        }
    }
}
