package com.example.stb.komunikacija;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientRunnable implements Runnable {
    private static final String TAG = ClientRunnable.class.getSimpleName();

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private RemoteControlService rcs;

    public ClientRunnable(Socket socket, RemoteControlService s) {
        this.socket = socket;
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        rcs = s;
    }

    @Override
    public void run() {
        String messageTmp = null;
        try {
            while (socket != null && socket.isConnected() && (messageTmp = in.readLine()) != null) {
               // Log.d(TAG, "receive from client " + socket.hashCode() + ": " + messageTmp);
                 Log.d("juliam", "receive from client " + socket.hashCode() + ": " + messageTmp);
                final String message = messageTmp;
                // Send the key command to activities that listen to the service
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (message.equals(Commands.MOVE_UP)) rcs.sendMessageToUI(RemoteControlService.CMD__MOVE_UP, null);
                        else if (message.equals(Commands.MOVE_DOWN)) rcs.sendMessageToUI(RemoteControlService.CMD__MOVE_DOWN, null);
                        else if (message.equals(Commands.SOUND_PLUS)) rcs.sendMessageToUI(RemoteControlService.CMD__SOUND_PLUS, null);
                        else if (message.equals(Commands.SOUND_MINUS)) rcs.sendMessageToUI(RemoteControlService.CMD__SOUND_MINUS, null);
                    }
                });
                sendMessage(message + "__ok");
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        } finally {
            try {
                in.close();
                out.close();
                socket.close();
            } catch (IOException e) {
                Log.e("juliam", e.getMessage());
            }
        }
    }

    public void sendMessage(final String msg) {
        try {
            out.println(msg);
            Log.d("juliam", "send to client " + socket.hashCode() + ": " + msg);
            
        } catch (Exception e) {
            Log.e("juliam", "ERROR:", e);
        }
    }
}