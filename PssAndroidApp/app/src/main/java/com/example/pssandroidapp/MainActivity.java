package com.example.pssandroidapp;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiInfo;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.compose.ui.graphics.DegreesKt;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pssandroidapp.API.Device;
import com.example.pssandroidapp.API.DeviceAPI;
import com.example.pssandroidapp.API.RequestCallback;
import com.example.pssandroidapp.DeviceList.DeviceListAdapter;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private DeviceAPI api;
    List<Device> devices = new ArrayList<>();
    RecyclerView view;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        view = findViewById(R.id.recyclerView);
        view.setLayoutManager(new LinearLayoutManager(this));
        view.setAdapter(new DeviceListAdapter(getApplicationContext(), devices));

        Button reloadBut = findViewById(R.id.reloadBut);
        reloadBut.setOnClickListener((x) -> {
            loadDeviceFromAP();
            //loadDevicesOnNetwork();
        });

        loadDeviceFromAP();
        //loadDevicesOnNetwork();
    }

    public void loadDeviceFromAP() {
        loadDevice("192.168.4.1");
    }

    public void loadDevicesOnNetwork() {
        String ip = getIPAddress(getApplicationContext());
        if(ip == null) {
            return;
        }
        List<String> activeIPs = scanNetwork(ip.substring(0,7));

        for(String netIP : activeIPs) {
            netIP = "http://" + netIP + ":" + DeviceAPI.DEVICE_PORT;
            DeviceAPI api = new DeviceAPI(netIP, getApplicationContext());
            Device device = new Device(netIP);
            api.getConfig(new RequestCallback() {
                @Override
                public void onSuccess(String response) {
                    addDevice(device);
                }

                @Override
                public void onError(Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public List<String> scanNetwork(String subnet) {
        List<String> activeIPs = new ArrayList<>();

        for (int i = 1; i < 255; i++) {
            String host = subnet + "." + i;
            try {
                if (InetAddress.getByName(host).isReachable(100)) { // 100ms timeout
                    activeIPs.add(host);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return activeIPs;
    }

    private String getIPAddress(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int ipAddress = wifiInfo.getIpAddress();
            return formatIpAddress(ipAddress);
        }
        return null;
    }
    private String formatIpAddress(int ipAddress) {
        return String.format("%d.%d.%d.%d",
                (ipAddress & 0xff),
                (ipAddress >> 8 & 0xff),
                (ipAddress >> 16 & 0xff),
                (ipAddress >> 24 & 0xff));
    }

    public void loadDevice(String url) {
        url = "http://" + url + ":" + DeviceAPI.DEVICE_PORT;
        Device device = new Device(url);
        DeviceAPI api = new DeviceAPI(url, getApplicationContext());
        device.updateConfig(api, new RequestCallback() {
            @Override
            public void onSuccess(String response) {
                addDevice(device);
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void addDevice(Device device) {
        if(!devices.contains(device)) {
            devices.add(device);
        }
        view.setAdapter(new DeviceListAdapter(getApplicationContext(), devices));
    }
}