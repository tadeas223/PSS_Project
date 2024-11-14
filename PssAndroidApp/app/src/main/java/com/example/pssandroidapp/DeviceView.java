package com.example.pssandroidapp;

import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pssandroidapp.API.Device;

public class DeviceView extends RecyclerView.ViewHolder {
    private final Button button;
    public DeviceView(@NonNull View itemView) {
        super(itemView);
        button = itemView.findViewById(R.id.button);
    }

    public Button getButton() {
        return button;
    }
}
