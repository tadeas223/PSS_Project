package com.example.pssandroidapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.pssandroidapp.API.Configuration;
import com.example.pssandroidapp.API.Device;
import com.example.pssandroidapp.API.DeviceAPI;
import com.example.pssandroidapp.API.RequestCallback;

import org.json.JSONException;
import org.json.JSONObject;

public class ConfigureActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure);
        String deviceUrl = getIntent().getStringExtra("device");

        Button apply = findViewById(R.id.configBut);
        Button back = findViewById(R.id.backBut2);

        EditText name = findViewById(R.id.nameText);
        EditText apSsid = findViewById(R.id.ap_ssidText);
        EditText apPasswd = findViewById(R.id.ap_passwdText);

        DeviceAPI api = new DeviceAPI(deviceUrl, getApplicationContext());

        final int[] mode = {0};
        final String[] wifiSsid = {""};
        final String[] wifiPasswd = {""};

        api.getConfig(new RequestCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject json = new JSONObject(response);

                    mode[0] = json.getInt("mode");
                    wifiSsid[0] = json.getString("wifi_ssid");
                    wifiPasswd[0] = json.getString("wifi_passwd");
                    name.setText(json.getString("name"));
                    apSsid.setText(json.getString("ap_ssid"));
                    apPasswd.setText(json.getString("ap_passwd"));

                    ((TextView)findViewById(R.id.deviceName2)).setText(json.getString("name"));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Exception e) {

            }
        });

        apply.setOnClickListener((event) -> {

            Configuration configuration = new Configuration(
                    name.getText().toString(),
                    mode[0],
                    apSsid.getText().toString(),
                    apPasswd.getText().toString(),
                    wifiSsid[0],
                    wifiPasswd[0]
            );

            api.setConfig(configuration, (error) -> {
                // TODO: :(
                error.printStackTrace();
            });

            startActivity(new Intent(ConfigureActivity.this, MainActivity.class));
        });

        back.setOnClickListener((event) -> {
            startActivity(new Intent(ConfigureActivity.this, MainActivity.class));
        });
    }
}