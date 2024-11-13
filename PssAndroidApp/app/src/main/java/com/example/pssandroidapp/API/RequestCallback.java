package com.example.pssandroidapp.API;

import org.json.JSONObject;

public interface RequestCallback {
    void onSuccess(JSONObject json);
    void onError(Exception e);
}
