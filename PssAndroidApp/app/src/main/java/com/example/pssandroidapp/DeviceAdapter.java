package com.example.pssandroidapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pssandroidapp.API.Device;

import java.util.List;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceView> {

    Context context;
    List<Device> devices;

    public DeviceAdapter(Context context, List<Device> devices) {
        this.context = context;
        this.devices = devices;
    }

    @NonNull
    @Override
    public DeviceView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DeviceView(LayoutInflater.from(context).inflate(R.layout.device_view, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceView holder, int position) {
        holder.getButton().setText(devices.get(position).getConfiguration().getName());
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }
}
