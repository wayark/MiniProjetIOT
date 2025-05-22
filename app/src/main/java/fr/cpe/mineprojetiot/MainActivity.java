package fr.cpe.mineprojetiot;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
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

    private CaptorAdapter adapter;
    private MyThreadSender myThread;

    private MyThreadReceiver myThreadReceiver;


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

        List<String> captors = new ArrayList<>(Arrays.asList("temperature", "humidity", "luminosity", "pressure"));
        adapter = new CaptorAdapter(captors);

        RecyclerView recyclerView = findViewById(R.id.captorSelector);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(RecyclerView recyclerView,
                                  RecyclerView.ViewHolder viewHolder,
                                  RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();
                adapter.moveItem(fromPosition, toPosition);
                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                // Not used
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        // get ip from connection activity
        Intent intent = getIntent();
        String ip = intent.getStringExtra("server_ip");
        String port = intent.getStringExtra("server_port");

        if (ip == null || port == null) {
            //return to connection activity
            Intent connectionIntent = new Intent(this, ConnectionActivity.class);
            startActivity(connectionIntent);
            finish();
            return;
        }
        myThread = new MyThreadSender(ip, Integer.parseInt(port));
        myThread.start();

        // send message to server
        Button getOrderButton = findViewById(R.id.getCaptorOrderButton);
        getOrderButton.setOnClickListener(v -> {
            List<String> order = adapter.getCaptorOrder();
            StringBuilder orderString = new StringBuilder();
            for (String captor : order) {
                String captorInitial = captor.substring(0, 1).toUpperCase();
                orderString.append(captorInitial).append("");
            }
            System.out.println(orderString.toString());
            myThread.sendMessage(orderString.toString());
        });

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
    public void updateOrCreateSquare(SensorData data) {
        LinearLayout container = findViewById(R.id.sensorSquaresContainer);

        // Try to find existing square by tag (ID)
        View existing = container.findViewWithTag("microbit_" + data.id);

        if (existing != null) {
            // Update values
            LinearLayout square = (LinearLayout) existing;
            ((TextView) square.findViewWithTag("temp")).setText("Temperature: " + data.temperature + "°C");
            ((TextView) square.findViewWithTag("hum")).setText("Humidity: " + data.humidity + "%");
            ((TextView) square.findViewWithTag("lum")).setText("Luminosity: " + data.luminosity + " lux");
            ((TextView) square.findViewWithTag("press")).setText("Pressure: " + data.pressure + " hPa");
        } else {
            // Create new square
            LinearLayout square = new LinearLayout(this);
            square.setOrientation(LinearLayout.VERTICAL);
            square.setPadding(16, 16, 16, 16);
            square.setBackgroundResource(R.drawable.rounded_square);
            square.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            square.setElevation(8);
            square.setTag("microbit_" + data.id); // Important: unique tag

            // Title
            TextView title = new TextView(this);
            title.setText("Micro:bit ID: " + data.id);
            title.setTextSize(18);
            title.setPadding(0, 0, 0, 8);
            square.addView(title);

            // Temperature
            TextView temp = new TextView(this);
            temp.setText("🌡️: " + data.temperature + "°C");
            temp.setTag("temp");
            square.addView(temp);

            // Humidity
            TextView hum = new TextView(this);
            hum.setText("🧊: " + data.humidity + "%");
            hum.setTag("hum");
            square.addView(hum);

            // Luminosity
            TextView lum = new TextView(this);
            lum.setText("💡: " + data.luminosity + " lux");
            lum.setTag("lum");
            square.addView(lum);

            // Pressure
            TextView press = new TextView(this);
            press.setText("🏋️: " + data.pressure + " hPa");
            press.setTag("press");
            square.addView(press);

            // Add square to container
            container.addView(square);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myThreadReceiver.stopThread();
        myThreadReceiver.interrupt();
        myThread.interrupt();
    }
}

