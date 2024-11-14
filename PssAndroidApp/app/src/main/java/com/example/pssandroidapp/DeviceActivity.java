package com.example.pssandroidapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Button;
import com.example.pssandroidapp.API.Device;
import com.example.pssandroidapp.API.DeviceAPI;
import com.example.pssandroidapp.API.NoResponseRequestCallback;

import androidx.annotation.Nullable;

public class DeviceActivity extends Activity {

    private Device device;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        device = getIntent().getParcelableExtra("device", Device.class);
        setContentView(R.layout.activity_device);

        Switch sw = (Switch) findViewById(R.id.switch1);
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                DeviceAPI api = new DeviceAPI(device.getUrl(), getApplicationContext());
                api.setPinState(b, new NoResponseRequestCallback() {
                    @Override
                    public void onError(Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        });

        Button configBut = findViewById(R.id.configureBut);
        configBut.setOnClickListener((x) -> {
            startActivity(new Intent(getApplicationContext(), ConfigureActivity.class));
        });

        Button backBut = findViewById(R.id.backBut);
        backBut.setOnClickListener((x) -> {
            startActivity(new Intent(DeviceActivity.this, MainActivity.class));
        });
    }
}
