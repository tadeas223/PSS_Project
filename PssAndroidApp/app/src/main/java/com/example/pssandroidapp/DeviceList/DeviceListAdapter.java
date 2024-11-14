package com.example.pssandroidapp.DeviceList;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pssandroidapp.API.Device;
import com.example.pssandroidapp.DeviceActivity;
import com.example.pssandroidapp.R;

import java.util.List;

public class DeviceListAdapter extends RecyclerView.Adapter<DeviceItemView> {

    Context context;
    List<Device> devices;

    public DeviceListAdapter(Context context, List<Device> devices) {
        this.context = context;
        this.devices = devices;
    }

    @NonNull
    @Override
    public DeviceItemView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DeviceItemView(LayoutInflater.from(context).inflate(R.layout.device_item_view, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceItemView holder, int position) {
        holder.getButton().setText(devices.get(position).getConfiguration().getName());
        holder.setDevice(devices.get(position));
        holder.setContext(context);
        bind(holder.getButton(), position);
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    public void bind(Button button, int position) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context,DeviceActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("device", devices.get(position));
                context.startActivity(intent);
            }
        });
    }
}
