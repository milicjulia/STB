package com.example.stb;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.example.stb.komunikacija.Commands;

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
    private String TAG = "MainActivity";
    private static  UUID myuuid= UUID.fromString("0000110a-0100-1000-8000-20805f9b34fb");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        stbrcc = new STBRemoteControlCommunication(this);
        stbrcc.doBindService();
        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Log.d("ERROR", "Permission1");
                return;
            }
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        AcceptThread connect = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            connect = new AcceptThread();
            connect.start();
        }

    }


    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code.
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord("STB",myuuid);
            } catch (IOException e) {
                Log.e(TAG, "Socket's listen() method failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run() {
            Log.d("Acceptocket", "Run");
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned.
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                    Log.d("Acceptocket", "accept");
                } catch (IOException e) {
                    Log.e(TAG, "Socket's accept() method failed", e);
                  return;
                }
                new ConnectedThread(socket).start();
            }
        }

        // Closes the connect socket and causes the thread to finish.
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
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
            try{
                Log.d("Command","chUp");
                Runtime.getRuntime().exec("input keyevent "+ Commands.MOVE_UP);
            }catch(Exception e){}

        }

        public void chDown() {
            try{
                Log.d("Command","chDown");
                Runtime.getRuntime().exec("input keyevent "+ Commands.MOVE_DOWN);
            }catch(Exception e){}
        }

        public void volUp() {
            try{
                Log.d("Command","volUp");
                Runtime.getRuntime().exec("input keyevent "+ Commands.SOUND_PLUS);
            }catch(Exception e){}
        }

        public void volDown() {
            try{
                Log.d("Command","volDown");
                Runtime.getRuntime().exec("input keyevent "+ Commands.SOUND_MINUS);
            }catch(Exception e){}
        }

        public void play(int command) {
            Log.d("command", "play 8->1");
            int toChannel = command - (int) (command / 10);

        }

        public void run() {
            int bytes;
            while (true) {
                try {
                    bytes = mmInStream.read();
                    switch (bytes) {
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
                            play(bytes);
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