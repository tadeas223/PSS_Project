package com.example.pssandroidapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Button;
import android.widget.TextView;

import com.example.pssandroidapp.API.Device;
import com.example.pssandroidapp.API.DeviceAPI;
import com.example.pssandroidapp.API.NoResponseRequestCallback;
import com.example.pssandroidapp.API.RequestCallback;

import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

public class DeviceActivity extends Activity {

    private String deviceUrl;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        deviceUrl = getIntent().getStringExtra("device");
        DeviceAPI api = new DeviceAPI(deviceUrl, getApplicationContext());
        setContentView(R.layout.activity_device);


        api.getConfig(new RequestCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject json = new JSONObject(response);
                    ((TextView)findViewById(R.id.deviceName)).setText(json.getString("name"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Exception e) {

            }
        });

        Switch sw = (Switch) findViewById(R.id.switch1);
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                api.setPinState(!b, new NoResponseRequestCallback() {
                    @Override
                    public void onError(Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        });

        Button configBut = findViewById(R.id.configureBut);
        configBut.setOnClickListener((x) -> {
            Intent intent = new Intent(getApplicationContext(), ConfigureActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("device", deviceUrl);
            getApplicationContext().startActivity(intent);
        });

        Button backBut = findViewById(R.id.backBut);
        backBut.setOnClickListener((x) -> {
            startActivity(new Intent(DeviceActivity.this, MainActivity.class));
        });
    }
}