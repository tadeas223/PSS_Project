package com.example.pssandroidapp;

import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pssandroidapp.API.DeviceAPI;
import com.example.pssandroidapp.API.RequestCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private DeviceAPI api;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        api = new DeviceAPI("http://192.168.4.1", this.getApplicationContext());

        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(e -> {
            System.out.println("Button clicked");
            api.getPinState(new RequestCallback() {
                @Override
                public void onSuccess(JSONObject json) {
                    try {
                        api.setPinState(!json.getBoolean("value"), new RequestCallback() {
                            @Override
                            public void onSuccess(JSONObject json) {
                                try {
                                    System.out.println(json.getString("message"));
                                } catch (JSONException ex) {
                                    throw new RuntimeException(ex);
                                }
                            }

                            @Override
                            public void onError(Exception e) {
                                System.out.println(e);
                            }
                        });
                    } catch (JSONException ex) {
                        throw new RuntimeException(ex);
                    }
                }

                @Override
                public void onError(Exception e) {
                    System.out.println(e);
                }
            });

        });

    }
}

