package fr.cpe.mineprojetiot;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private CaptorAdapter adapter;
    private MyThreadSender myThread;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
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
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        TextView temperatureTextView = findViewById(R.id.temperature);
                        TextView humidityTextView = findViewById(R.id.humidity);
                        TextView luminosityTextView = findViewById(R.id.luminosity);
                        TextView pressureTextView = findViewById(R.id.pressure);
                    }
                });

            }
        };

//        MyThreadReceiver myThreadReceiver = new MyThreadReceiver(listener, Integer.parseInt(port));
//        myThreadReceiver.start();

    }
}