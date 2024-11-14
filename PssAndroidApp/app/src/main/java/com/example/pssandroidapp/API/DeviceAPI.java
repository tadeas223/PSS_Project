package com.example.pssandroidapp.API;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class DeviceAPI {
    private String url;
    private Context context;
    public DeviceAPI(String url, Context context) {
        this.url = url;
        this.context = context;
    }

    public void getPinState(RequestCallback callback) {
        StringRequest request = new StringRequest(Request.Method.GET, url + "/control/status", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                callback.onSuccess(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callback.onError(error);
            }
        });
        ConnectionHandler.getInstance(context).addToRequestQueue(request);
    }

    public void setPinState(boolean value,NoResponseRequestCallback callback) {
        JSONObject json = new JSONObject();
        try {
            json.put("value", value);
        } catch (JSONException e) {
            callback.onError(e);
        }

        String requestBody = json.toString();
        System.out.println(requestBody);
        StringRequest request = new StringRequest(Request.Method.POST, url + "/control/value", new Response.Listener<String>() {
            public void onResponse(String response) {
                // Nothing to do here
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callback.onError(error);
            }
        }) {
            @Override
            public String getBodyContentType() {
                return "application/json";
            }

            @Override
            public byte[] getBody() {
                return requestBody.getBytes(StandardCharsets.UTF_8);
            }
        };

        ConnectionHandler.getInstance(context).addToRequestQueue(request);
    }

    public void ping(RequestCallback callback) {
        StringRequest request = new StringRequest(Request.Method.GET, url + "/helloworld", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                callback.onSuccess(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callback.onError(error);
            }
        });
        ConnectionHandler.getInstance(context).addToRequestQueue(request);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void getConfig(RequestCallback callback) {
        StringRequest request = new StringRequest(Request.Method.GET, url + "/config", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                callback.onSuccess(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callback.onError(error);
            }
        });

        ConnectionHandler.getInstance(context).addToRequestQueue(request);
    }
}

