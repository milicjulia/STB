package com.example.stb.komunikacija;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerRunnable implements Runnable {
    private static final String TAG = "juliam";

    private int port;
    private RemoteControlService rcs;

    public ServerRunnable(RemoteControlService serv, int communicationPort) {
        port = communicationPort;
        rcs = serv;
        Log.v(TAG, "ServerRunnable constructor");
    }

    @Override
    public void run() {
        try {
            Log.i(TAG, "Starting server...");

            ServerSocket s = new ServerSocket(port);
            s.setReuseAddress(true);

            Log.i(TAG, "Server is listening");

            while (s != null) {

                final Socket sock_client = s.accept();
                Log.i(TAG, "new client: "+sock_client.hashCode());
                // launch a new thread for each new client - This thread will handle client commands/request from the remote control
                ClientRunnable client_runnable = new ClientRunnable(sock_client, rcs);
                Thread client_thread = new Thread(client_runnable);
                client_thread.start();
            }

        } catch (Exception e) {
            Log.e(TAG, "ERROR:\n", e);
        }
    }
}