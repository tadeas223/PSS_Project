package com.example.pssandroidapp.API;

import android.content.Context;

import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.Timestamp;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class Device {
    private final DeviceAPI api;

    private final String url;
    private Configuration configuration;

    public Device(String url, Context context) {
        this.url = url;
        api = new DeviceAPI(url, context);
    }

    public void updateConfig(RequestCallback callback) {
        api.getConfig(new RequestCallback() {
            @Override
            public void onSuccess(String response) {
                JSONObject json = null;
                try {
                    json = new JSONObject(response);
                    configuration = new Configuration(json);
                } catch (JSONException e) {
                    callback.onError(e);
                    return;
                }
                callback.onSuccess(response);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    public String getUrl() {
        return url;
    }

    public DeviceAPI getApi() {
        return api;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public boolean equals(@Nullable Object obj){
        if(!(obj instanceof Device)) {
            return false;
        }
        return url.equals(((Device) obj).url);
    }
}
