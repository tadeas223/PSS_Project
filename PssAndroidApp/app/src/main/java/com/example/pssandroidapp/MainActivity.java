package com.example.pssandroidapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ScrollView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pssandroidapp.API.Device;
import com.example.pssandroidapp.API.DeviceAPI;
import com.example.pssandroidapp.API.RequestCallback;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private DeviceAPI api;
    List<Device> devices = new ArrayList<>();
    RecyclerView view;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        view = findViewById(R.id.recyclerView);
        view.setLayoutManager(new LinearLayoutManager(this));
        view.setAdapter(new DeviceAdapter(getApplicationContext(), devices));

        Button reloadBut = findViewById(R.id.reloadBut);
        reloadBut.setOnClickListener((x) -> {
            String url = "http://192.168.4.1";
            api = new DeviceAPI(url, this.getApplicationContext());
            api.ping(new RequestCallback() {
                @Override
                public void onSuccess(String response) {
                    loadDevice(url);
                }

                @Override
                public void onError(Exception e) {
                    e.printStackTrace();
                }
            });
        });
    }

    public void loadDevice(String url) {
        Device device = new Device(url, getApplicationContext());

        device.updateConfig(new RequestCallback() {
            @Override
            public void onSuccess(String response) {
                if(!devices.contains(device)) {
                    devices.add(device);
                }
                view.setAdapter(new DeviceAdapter(getApplicationContext(), devices));
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });
    }
}

