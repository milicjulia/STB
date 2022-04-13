package com.example.stb;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.stb.komunikacija.BootStart;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends Activity {
    public static final int MESSAGE_DEVICE_NAME = 1;
    public static final int MESSAGE_WRITE = 2;
    public static final int MESSAGE_DISCONNECTED = 3;
    public static final int MESSAGE_READ = 4;
    private BluetoothAdapter BA;
    private Set<BluetoothDevice> pairedDevices;
    public final static int COMMUNICATION_PORT = 2000;

    STBRemoteControlCommunication stbrcc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        stbrcc = new STBRemoteControlCommunication(this);
        Log.d("main","1");
        stbrcc.doBindService();
        new BootStart();
        Log.d("main","2");
        BA = BluetoothAdapter.getDefaultAdapter();
        on();
        visible();
        list();

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
        Log.d("ok", "visible");
        startActivityForResult(getVisible, 0);
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
        Log.d("ok", "list");
        int i=0;
        for (BluetoothDevice bt : pairedDevices) {
            Log.d("device", bt.getName());
            if(i==0) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    bt.createBond();
                    bt.connectGatt(getApplicationContext(), true, new BluetoothGattCallback() {
                        @Override
                        public void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
                            super.onPhyUpdate(gatt, txPhy, rxPhy, status);
                        }

                        @Override
                        public void onPhyRead(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
                            super.onPhyRead(gatt, txPhy, rxPhy, status);
                        }

                        @Override
                        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                            super.onConnectionStateChange(gatt, status, newState);
                            Log.d("conn", String.valueOf(status));

                        }

                        @Override
                        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                            super.onServicesDiscovered(gatt, status);
                        }

                        @Override
                        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                            super.onCharacteristicRead(gatt, characteristic, status);
                        }

                        @Override
                        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                            super.onCharacteristicWrite(gatt, characteristic, status);
                        }

                        @Override
                        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                            super.onCharacteristicChanged(gatt, characteristic);
                        }

                        @Override
                        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                            super.onDescriptorRead(gatt, descriptor, status);
                        }

                        @Override
                        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                            super.onDescriptorWrite(gatt, descriptor, status);
                        }

                        @Override
                        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
                            super.onReliableWriteCompleted(gatt, status);
                        }

                        @Override
                        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
                            super.onReadRemoteRssi(gatt, rssi, status);
                        }

                        @Override
                        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
                            super.onMtuChanged(gatt, mtu, status);
                        }

                        @Override
                        public void onServiceChanged(@NonNull BluetoothGatt gatt) {
                            super.onServiceChanged(gatt);
                        }
                    });
                }
            }
            i++;
        }
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2){

        }

    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();
        this.off();
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }
}