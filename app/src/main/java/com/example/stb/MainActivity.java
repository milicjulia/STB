package com.example.stb;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.ArrayAdapter;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends Activity {
    private BluetoothAdapter BA;
    private Set<BluetoothDevice> pairedDevices;
    private BluetoothSocket BS;
    public final static int COMMUNICATION_PORT = 2000;
    private static UUID uid=UUID.randomUUID();
    STBRemoteControlCommunication stbrcc;
    private InputStream input;
    private OutputStream output;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        stbrcc = new STBRemoteControlCommunication(this);
        stbrcc.doBindService();
        startBluetooth();

    }

    public void startBluetooth(){
        BA = BluetoothAdapter.getDefaultAdapter();
        BS = null;
        on();
        visible();
        list();
    }

    public void initSocket(BluetoothDevice device){
        try {
            BS = device.createRfcommSocketToServiceRecord(uid);
        } catch (IOException e) {
            Log.d("error", "initSocket");
        }
    }

    public void getSocketStreams(){
        try {
            input = BS.getInputStream();
            output =  BS.getOutputStream();
        } catch (IOException e) {
            Log.d("error", "getSocketStreams");
        }
    }

    public void receivingData(){
        byte[] buffer = new byte[1024];
        int bytesCount;

        while (true) {
            try {
                bytesCount = input.read(buffer);
                if(buffer != null && bytesCount > 0) {
                    Log.d("OK", "receivingData");
                }
            } catch (IOException e) {
                Log.d("error", "receivingData");
            }
        }
    }

    public void sendingData(byte[] bytes){
        try {
            output.write(bytes);
            Log.d("OK", "sendingData");
        } catch (IOException e) {
            Log.d("error", "sendingData");
        }
    }

    public void connectSocket(){
        try {
            BS.connect();
        } catch (IOException connEx) {
            try {
                BS.close();
            } catch (IOException closeException) {
                Log.d("error", "connectSocket");
            }
        }

        if (BS != null && BS.isConnected()) {
            Log.d("OK", "connectSocket");
        }
    }

    public void on() {
        if (!BA.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Log.d("error", "ON");
                return;
            }
            startActivityForResult(turnOn, 0);
            Log.d("ok", "ON");
        } else {
            Log.d("ok", "already ON");
        }
    }

    public void off() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.d("error", "OFF");
            return;
        }
        BA.disable();
        Log.d("ok", "OFF");
    }

    public void visible() {
        Intent getVisible = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 2);
                Log.d("error", "visible");
                return;
            }
        }
        Log.d("OK", "visible");
        startActivityForResult(getVisible, 1001);


    }

    public void list() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 2);
                Log.d("error", "list");
                return;
            }
        }
        pairedDevices = BA.getBondedDevices();
        int i = 0;
        for (BluetoothDevice bt : pairedDevices) {
            if (i == 0) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    BluetoothDevice device = BA.getRemoteDevice(bt.getAddress());
                    device.createBond();
                    BA.cancelDiscovery();
                    initSocket(device);
                    connectSocket();
                    getSocketStreams();
                }
            }
            i++;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();
        off();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}