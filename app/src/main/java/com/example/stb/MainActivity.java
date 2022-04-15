package com.example.stb;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothServerSocket;
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
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends Activity {
    public static BluetoothAdapter BA;
    private Set<BluetoothDevice> pairedDevices;
    private BluetoothSocket BS;
    BluetoothServerSocket mServerSocket;
    public final static int COMMUNICATION_PORT = 2000;
    private static UUID uid=null;
    STBRemoteControlCommunication stbrcc;
    public static InputStream input=null;
    public static OutputStream output=null;
    private BThread bthread=null;


    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    private static final int REQUEST_ENABLE_BT = 1;
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothDevice mmDevice;
    private UUID deviceUUID;
    ConnectedThread mConnectedThread;
    private Handler handler;

    String TAG = "MainActivity";
    StringBuilder messages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        stbrcc = new STBRemoteControlCommunication(this);
        stbrcc.doBindService();
       // startBluetooth();
        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new
                    Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

        }pairDevice();

    }




    public void pairDevice() {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        Log.e("MAinActivity", "" + pairedDevices.size() );
        if (pairedDevices.size() > 0) {
            Object[] devices = pairedDevices.toArray();
            BluetoothDevice device = (BluetoothDevice) devices[0];
            Log.e("MAinActivity", "" + device.getName() );
            ConnectThread connect = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                connect = new ConnectThread(device,
                        device.getUuids()[0].getUuid());
            }
            connect.start();

        }
    }



    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;

        public ConnectThread(BluetoothDevice device, UUID uuid) {
            Log.d(TAG, "ConnectThread: started.");
            mmDevice = device;
            deviceUUID = uuid;
        }

        public void run(){
            BluetoothSocket tmp = null;
            Log.i(TAG, "RUN mConnectThread ");

            try {
                 if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                     Method createInsecureRfcommSocket = mmDevice.getClass().getMethod("createInsecureRfcommSocket", new Class[]{int.class});
                     mmSocket = (BluetoothSocket)createInsecureRfcommSocket.invoke(mmDevice, 1);
                 }
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            try {
                mmSocket.connect();
            } catch (IOException e) {
                try {
                    mmSocket.close();
                    Log.d(TAG, "run: Closed Socket.");
                } catch (IOException e1) {
                    Log.e(TAG, "mConnectThread: run: Unable to close connection in socket " + e1.getMessage());
                }
                e.printStackTrace();
                return;
            }
            Log.d(TAG, "run: ide dalje.");
            //will talk about this in the 3rd video
            connected(mmSocket);
        }
        public void cancel() {
            try {
                Log.d(TAG, "cancel: Closing Client Socket.");
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel: close() of mmSocket in Connectthread failed. " + e.getMessage());
            }
        }
    }

    private void connected(BluetoothSocket mmSocket) {
        Log.d(TAG, "connected: Starting.");
        mConnectedThread = new ConnectedThread(mmSocket);
        mConnectedThread.start();
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "ConnectedThread: Starting.");

            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void chUp(){

        }

        public void chDown(){

        }

        public void volUp(){

        }

        public void volDown(){

        }

        public void play(int command){
            int toChannel=command-(int)(command/10);
        }

        public void run(){
            byte[] buffer = new byte[1024];
            int bytes;
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);
                    final String incomingMessage = new String(buffer, 0, bytes);
                    switch(Integer.parseInt(incomingMessage)){
                        case 1: chUp(); break;
                        case 2: chDown(); break;
                        case 3: volUp(); break;
                        case 4: volDown(); break;
                        default: play(Integer.parseInt(incomingMessage)); break;
                    }
                } catch (IOException e) {
                    Log.e(TAG, "write: Error reading Input Stream. " + e.getMessage() );
                    break;
                }
            }
        }


        public void write(byte[] bytes) {
            String text = new String(bytes, Charset.defaultCharset());
            Log.d(TAG, "write: Writing to outputstream: " + text);
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e(TAG, "write: Error writing to output stream. " + e.getMessage() );
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }



/*
    public void startBluetooth(){
        BA = BluetoothAdapter.getDefaultAdapter();
        BS = null;
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
                    bthread=new BThread(device, BS);
                }
            }
            i++;
        }
    }
*/
    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();
        //off();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}