package fr.cpe.mineprojetiot;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private MyThreadReceiver myThreadReceiver;
    private String ip;
    private String port;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.primary));
        }
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // get ip from connection activity
        Intent intent = getIntent();
        this.ip = intent.getStringExtra("server_ip");
        this.port = intent.getStringExtra("server_port");

        if (ip == null || port == null) {
            //return to connection activity
            Intent connectionIntent = new Intent(this, ConnectionActivity.class);
            startActivity(connectionIntent);
            finish();
            return;
        }

        //receive message from server
        MyThreadEventListener listener = new MyThreadEventListener() {
            @Override
            public void onEventInMyThread(String data) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    SensorData sensorData = SensorData.fromString(data);
                    if(sensorData != null && sensorData.id != null) {
                        updateOrCreateSquare(sensorData);
                    }
                });

            }
        };


        myThreadReceiver = new MyThreadReceiver(listener, ip, Integer.parseInt(port));
        myThreadReceiver.start();

    }
    protected void updateOrCreateSquare(SensorData data) {
        LinearLayout container = findViewById(R.id.sensorSquaresContainer);

        // Try to find existing square by tag (ID)
        View existing = container.findViewWithTag("microbit_" + data.id);

        if (existing != null) {
            // Update values
            LinearLayout square = (LinearLayout) existing;
            if(data.temperature != null)
                ((TextView) square.findViewWithTag("temp")).setText("🌡️: " + data.temperature + "°C");
            if(data.humidity != null)
                ((TextView) square.findViewWithTag("hum")).setText("🧊: " + data.humidity + "%");
            if(data.luminosity != null)
                ((TextView) square.findViewWithTag("lum")).setText("💡: " + data.luminosity + " lux");
            if(data.pressure != null)
                ((TextView) square.findViewWithTag("press")).setText("🏋️: " + data.pressure + " hPa");
        } else {
            // Create new square
            LinearLayout square = new LinearLayout(this);
            square.setOrientation(LinearLayout.VERTICAL);
            square.setPadding(16, 16, 16, 16);
            square.setBackgroundResource(R.drawable.rounded_square);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(16, 16, 16, 16);
            square.setLayoutParams(params);
            square.setElevation(8);
            square.setTag("microbit_" + data.id); // Important: unique tag

            // Title
            TextView title = new TextView(this);
            title.setText("Microbit ID: " + data.id);
            title.setTextColor(getResources().getColor(R.color.white));
            title.setTextSize(18);
            title.setPadding(0, 0, 0, 8);
            square.addView(title);

            // Temperature
            TextView temp = new TextView(this);
            temp.setText("🌡️: " + data.temperature + "°C");
            title.setTextColor(getResources().getColor(R.color.white));
            temp.setTag("temp");
            square.addView(temp);

            // Humidity
            TextView hum = new TextView(this);
            hum.setText("🧊: " + data.humidity + "%");
            title.setTextColor(getResources().getColor(R.color.white));
            hum.setTag("hum");
            square.addView(hum);

            // Luminosity
            TextView lum = new TextView(this);
            lum.setText("💡: " + data.luminosity + " lux");
            title.setTextColor(getResources().getColor(R.color.white));
            lum.setTag("lum");
            square.addView(lum);

            // Pressure
            TextView press = new TextView(this);
            press.setText("🏋️: " + data.pressure + " hPa");
            title.setTextColor(getResources().getColor(R.color.white));
            press.setTag("press");
            square.addView(press);

            // Add square to container
            square.setOnClickListener(v -> {
                // Handle click event
                onSensorSquareClicked(data.id);
            });
            container.addView(square);
        }
    }

    protected void onSensorSquareClicked(String sensorId){
        // Optionally save values using SharedPreferences
        Intent intent = new Intent(MainActivity.this, SensorOrderActivity.class);
        intent.putExtra("server_ip", ip);
        intent.putExtra("server_port", port);
        intent.putExtra("sensor_id", sensorId);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myThreadReceiver.stopThread();
        myThreadReceiver.interrupt();
    }
}

