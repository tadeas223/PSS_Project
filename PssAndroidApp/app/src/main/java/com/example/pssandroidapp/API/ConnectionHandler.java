package com.example.pssandroidapp.API;

import android.content.Context;
import android.widget.NumberPicker;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class ConnectionHandler {
    private static ConnectionHandler instance;
    private RequestQueue requestQueue;

    public static ConnectionHandler getInstance(Context context) {
        if(instance == null) {
            instance = new ConnectionHandler();
        }
        if(instance.requestQueue == null) {
            instance.requestQueue = Volley.newRequestQueue(context);
        }
        return instance;
    }

    public RequestQueue getRequestQueue() {
        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }
}
