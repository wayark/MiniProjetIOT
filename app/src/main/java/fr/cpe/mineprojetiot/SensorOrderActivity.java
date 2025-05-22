package fr.cpe.mineprojetiot;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SensorOrderActivity extends AppCompatActivity {

    private CaptorAdapter adapter;
    private MyThreadSender myThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sensor_order);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        String ip = intent.getStringExtra("server_ip");
        String port = intent.getStringExtra("server_port");
        String sensorId = intent.getStringExtra("sensor_id");

        TextView sensorIdTextview = findViewById(R.id.microbitId);
        sensorIdTextview.setText("Microbit Id: " + sensorId);

        if (ip == null || port == null) {
            //return to connection activity
            Intent connectionIntent = new Intent(this, ConnectionActivity.class);
            startActivity(connectionIntent);
            finish();
            return;
        }
        myThread = new MyThreadSender(ip, Integer.parseInt(port));
        myThread.start();


        List<String> captors = new ArrayList<>(Arrays.asList("temperature 🌡️", "humidity 🧊", "pressure 🏋️", "luminosity 💡"));
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

        Button getOrderButton = findViewById(R.id.getCaptorOrderButton);
        getOrderButton.setOnClickListener(v -> {
            onSendButtonCLicked();
        });

    }

    protected void onSendButtonCLicked(){
        List<String> order = adapter.getCaptorOrder();
        StringBuilder orderString = new StringBuilder();
        for (String captor : order) {
            String captorInitial = captor.substring(0, 1).toUpperCase();
            orderString.append(captorInitial).append("");
        }
        System.out.println(orderString.toString());
        myThread.sendMessage(orderString.toString());
    }
}