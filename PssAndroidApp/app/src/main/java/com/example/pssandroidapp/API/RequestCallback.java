package com.example.pssandroidapp.API;

import org.json.JSONObject;

public interface RequestCallback {
    void onSuccess(String response);
    void onError(Exception e);
}
