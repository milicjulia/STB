package com.example.stb;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class BThread extends Thread{
    public static byte[] mmBuffer;
    private BluetoothDevice device;
    private BluetoothSocket socket;

    public BThread(BluetoothDevice device, BluetoothSocket socket){
        this.device=device;
        this.socket=socket;
    }

    public void initSocket(){
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                socket = device.createRfcommSocketToServiceRecord(UUID.fromString(String.valueOf(device.getUuids()[0])));
            }
            Log.d("OK", "initSocket");
        } catch (IOException e) {
            Log.d("error", "initSocket");
        }
    }


    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.d("Handler", "connected with device");
        }
    };


    public void getSocketStreams(){
        try {
            MainActivity.input = socket.getInputStream();
            MainActivity.output = socket.getOutputStream();
        } catch (IOException e) {
            Log.d("error", "getSocketStreams");
        }
    }

    public void receivingData(){
        byte[] buffer = new byte[1024];
        int bytesCount;

        while (true) {
            try {
                bytesCount = MainActivity.input.read(buffer);
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
            MainActivity.output.write(bytes);
            Log.d("OK", "sendingData");
        } catch (IOException e) {
            Log.d("error", "sendingData");
        }
    }

    public void connectSocket(){
        try {
            socket.connect();
            Log.d("OK", "nesto");
        } catch (IOException connEx) {
            try {
                connEx.printStackTrace();
                socket.close();
                Log.d("closing", "connectSocket");
            } catch (IOException closeException) {
                Log.d("error", "connectSocket");
            }
        }

        if (socket != null && socket.isConnected()) {
            Log.d("OK", "connectSocket");
        }
    }


    @Override
    public void run() {
        initSocket();
        getSocketStreams();
        connectSocket();
       while(socket.isConnected()) {
           mmBuffer = new byte[1024];
           int numBytes; // bytes returned from read()

           // Keep listening to the InputStream until an exception occurs.
           while (true) {
               try {
                   numBytes = MainActivity.input.read(mmBuffer);
                   Message readMsg = mHandler.obtainMessage(0, numBytes, -1, mmBuffer);
                   readMsg.sendToTarget();
               } catch (IOException e) {
                   Log.d("error", "Input stream was disconnected", e);
                   break;
               }
           }
       }


    }
}
