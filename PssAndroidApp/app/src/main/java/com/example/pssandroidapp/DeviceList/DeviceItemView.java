package com.example.pssandroidapp.DeviceList;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pssandroidapp.API.Device;
import com.example.pssandroidapp.DeviceActivity;
import com.example.pssandroidapp.R;

public class DeviceItemView extends RecyclerView.ViewHolder {
    private final Button button;
    private Device device;
    private Context context;
    public DeviceItemView(@NonNull View itemView) {
        super(itemView);
        button = itemView.findViewById(R.id.button);
    }

    public Button getButton() {
        return button;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
