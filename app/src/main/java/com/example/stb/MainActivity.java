package com.example.stb;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends Activity {
    public final static int COMMUNICATION_PORT = 2000;
    private STBRemoteControlCommunication stbrcc;
    private static final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothDevice mmDevice;
    private ConnectedThread mConnectedThread;
    private String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        stbrcc = new STBRemoteControlCommunication(this);
        stbrcc.doBindService();
        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        pairDevice();
    }

    public void pairDevice() {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            BluetoothDevice device = (BluetoothDevice) pairedDevices.toArray()[0];
            Log.e(TAG, "" + device.getName());
            ConnectThread connect = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                connect = new ConnectThread(device, device.getUuids()[0].getUuid());
            }
            connect.start();
        }
    }


    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;
        private String ConnectTag = "ConnectedThread";

        public ConnectThread(BluetoothDevice device, UUID uuid) {
            Log.d(ConnectTag, "Started.");
            mmDevice = device;
        }

        public void run() {
            Log.i(ConnectTag, "Run");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                try {
                    mmSocket = (BluetoothSocket)mmDevice.getClass().getMethod("createInsecureRfcommSocket", new Class[]{int.class}).invoke(mmDevice, 1);
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
            try {
                mmSocket.connect();
            } catch (IOException e) {
                try {
                    mmSocket.close();
                    Log.d(ConnectTag, "Closed Socket");
                } catch (IOException e1) {
                    Log.e(ConnectTag, "Unable to close socket connection");
                }
                e.printStackTrace();
                return;
            }
            connected(mmSocket);
        }
    }

    private void connected(BluetoothSocket mmSocket) {
        Log.d(TAG, "connected: Starting");
        mConnectedThread = new ConnectedThread(mmSocket);
        mConnectedThread.start();
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private String ConnectedTag="ConnectedThread";

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(ConnectedTag, "Starting");
            mmSocket = socket;
            InputStream tmpIn = null;

            try {
                tmpIn = mmSocket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmInStream = tmpIn;
        }

        public void chUp() {
            Log.d("command", "chup");
        }

        public void chDown() {
            Log.d("command", "chdown");
        }

        public void volUp() {
            Log.d("command", "volup");
        }

        public void volDown() {
            Log.d("command", "voldown");
        }

        public void play(int command) {
            Log.d("command", "play");
            int toChannel = command - (int) (command / 10);

        }

        public void run() {
            int bytes;
            while (true) {
                try {
                    bytes = mmInStream.read();
                    final String incomingMessage = new String(String.valueOf(bytes));
                    switch (Integer.parseInt(incomingMessage)) {
                        case 1:
                            chUp();
                            break;
                        case 2:
                            chDown();
                            break;
                        case 3:
                            volUp();
                            break;
                        case 4:
                            volDown();
                            break;
                        default:
                            play(Integer.parseInt(incomingMessage));
                            break;
                    }
                } catch (IOException e) {
                    Log.e(ConnectedTag, "write: Error reading Input Stream. " + e.getMessage());
                    break;
                }
            }
        }
    }


    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}