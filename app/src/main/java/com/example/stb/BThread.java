package com.example.stb;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

public class BThread extends Thread{
    public static byte[] mmBuffer;

    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.d("Handler", "connected with device");
        }
    };


    public void getSocketStreams(){
        try {
            MainActivity.input = MainActivity.BS.getInputStream();
            MainActivity.output = MainActivity.BS.getOutputStream();
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
            MainActivity.BS.connect();
            Log.d("OK", "nesto");
        } catch (IOException connEx) {
            try {
                connEx.printStackTrace();
                MainActivity.BS.close();
                Log.d("closing", "connectSocket");
            } catch (IOException closeException) {
                Log.d("error", "connectSocket");
            }
        }

        if (MainActivity.BS != null && MainActivity.BS.isConnected()) {
            Log.d("OK", "connectSocket");
        }
    }


    @Override
    public void run() {
        getSocketStreams();
        MainActivity.BA.cancelDiscovery();
        connectSocket();
       while(MainActivity.BS.isConnected()) {
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
