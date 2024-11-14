package com.example.pssandroidapp.API;

import org.json.JSONException;
import org.json.JSONObject;

public class Configuration {
    private String name;
    private int mode;
    private String ap_ssid;
    private String ap_passwd;
    private String wifi_ssid;
    private String wifi_passwd;

    public Configuration(JSONObject json) throws JSONException {
        fromJson(json);
    }
    public Configuration(String name, int mode, String ap_ssid, String ap_passwd, String wifi_ssid, String wifi_passwd) {
        this.name = name;
        this.mode = mode;
        this.ap_ssid = ap_ssid;
        this.ap_passwd = ap_passwd;
        this.wifi_ssid = wifi_ssid;
        this.wifi_passwd = wifi_passwd;
    }

    public void fromJson(JSONObject json) throws JSONException {
        this.name = json.getString("name");
        this.mode = json.getInt("mode");
        this.ap_ssid = json.getString("ap_ssid");
        this.ap_passwd = json.getString("ap_passwd");
        this.wifi_ssid = json.getString("wifi_ssid");
        this.wifi_passwd = json.getString("wifi_passwd");
    }

    public String getName() {
        return name;
    }

    public int getMode() {
        return mode;
    }

    public String getAp_ssid() {
        return ap_ssid;
    }

    public String getAp_passwd() {
        return ap_passwd;
    }

    public String getWifi_ssid() {
        return wifi_ssid;
    }

    public String getWifi_passwd() {
        return wifi_passwd;
    }
}
