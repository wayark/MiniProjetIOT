package fr.cpe.mineprojetiot;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ConnectionActivity extends AppCompatActivity {

    private EditText ipEditText, portEditText;
    private Button connectButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);

        ipEditText = findViewById(R.id.ipEditText);
        portEditText = findViewById(R.id.portEditText);
        connectButton = findViewById(R.id.connectButton);

        connectButton.setOnClickListener(v -> {
            String ip = ipEditText.getText().toString().trim();
            String port = portEditText.getText().toString().trim();

            if (ip.isEmpty() || port.isEmpty()) {
                Toast.makeText(this, "Please enter both IP and Port", Toast.LENGTH_SHORT).show();
                return;
            }

            // Optionally validate IP and port format here

            if(!port.matches("\\d+")) {
                Toast.makeText(this, "Port must be a number", Toast.LENGTH_SHORT).show();
                return;
            }


            // Optionally save values using SharedPreferences
            Intent intent = new Intent(ConnectionActivity.this, MainActivity.class);
            intent.putExtra("server_ip", ip);
            intent.putExtra("server_port", port);
            startActivity(intent);
            finish();
        });
    }
}
