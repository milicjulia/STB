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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends Activity {
    public static BluetoothAdapter BA;
    private Set<BluetoothDevice> pairedDevices;
    public static BluetoothSocket BS=null;
    public final static int COMMUNICATION_PORT = 2000;
    private static UUID uid=null;
    STBRemoteControlCommunication stbrcc;
    public static InputStream input=null;
    public static OutputStream output=null;
    BThread bthread=null;

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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                BS = device.createRfcommSocketToServiceRecord(UUID.fromString(String.valueOf(device.getUuids()[0])));
            }
            Log.d("OK", "initSocket");
        } catch (IOException e) {
            Log.d("error", "initSocket");
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

    public void write(byte[] bytes) {
        try {
            output.write(bytes);
            Message writtenMsg = bthread.mHandler.obtainMessage(1, -1, -1, bthread.mmBuffer);
            writtenMsg.sendToTarget();
        } catch (IOException e) {
            Log.e("write error", "Error occurred when sending data", e);

            // Send a failure message back to the activity.
            Message writeErrorMsg = bthread.mHandler.obtainMessage(2);
            Bundle bundle = new Bundle();
            bundle.putString("toast", "Couldn't send data to the other device");
            writeErrorMsg.setData(bundle);
            bthread.mHandler.sendMessage(writeErrorMsg);
        }
    }

    private boolean STATE_CONNECTING=false;
    public synchronized void connect() {

        // Cancel any thread currently running a connection
        if (bthread != null) {
            bthread.stop();
            bthread = null;
        }

        // Start the thread to connect with the given device
        bthread = new BThread();
        bthread.start();
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
                    try {
                        BS =(BluetoothSocket) device.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(device,1);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                    initSocket(device);
                    connect();
                  //  write();
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