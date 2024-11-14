package com.example.pssandroidapp.API;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

public class Configuration implements Parcelable {
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

    public Configuration(Parcel parcel) {
        String[] data = new String[5];
        parcel.readStringArray(data);
        this.name = data[0];
        this.ap_ssid = data[1];
        this.ap_passwd = data[2];
        this.wifi_ssid = data[3];
        this.wifi_passwd = data[4];
        this.mode = parcel.readInt();
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeStringArray(new String[] {
                this.name,
                this.ap_ssid,
                this.ap_passwd,
                this.wifi_ssid,
                this.wifi_passwd
        });
        parcel.writeInt(mode);
    }

    public static final Parcelable.Creator<Configuration> CREATOR = new Parcelable.Creator<Configuration>() {

        @Override
        public Configuration createFromParcel(Parcel parcel) {
            return new Configuration(parcel);
        }

        @Override
        public Configuration[] newArray(int i) {
            return new Configuration[i];
        }
    };
}
