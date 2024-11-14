package com.example.pssandroidapp.API;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.security.Timestamp;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class Device implements Parcelable {
    private final String url;
    private Configuration configuration;

    public Device(String url) {
        this.url = url;
    }

    public Device(Parcel parcel) {
        this.url = parcel.readString();
        this.configuration = parcel.readParcelable(Configuration.class.getClassLoader(), Configuration.class);
    }

    public void updateConfig(DeviceAPI api, RequestCallback callback) {
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(url);
        parcel.writeParcelable(configuration,0);
    }

    public static final Parcelable.Creator<Device> CREATOR = new Creator<Device>() {
        @Override
        public Device createFromParcel(Parcel parcel) {
            return new Device(parcel);
        }

        @Override
        public Device[] newArray(int i) {
            return new Device[i];
        }
    };
}
