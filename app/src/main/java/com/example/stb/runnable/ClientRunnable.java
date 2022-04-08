package com.example.stb.runnable;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.stb.komunikacija.Commands;
import com.example.stb.servis.RemoteControlService;

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
        String userTextTmp = null;
        boolean userTextInput = false;
        try {
            while (socket != null && socket.isConnected() && (messageTmp = in.readLine()) != null) {
                Log.d(TAG, "receive from client " + socket.hashCode() + ": " + messageTmp);
                userTextTmp = null;
                if ("Hello".equals(messageTmp)) {
                    sendMessage("World");
                } else if ("CLIENT_DISCONNECT".equals(messageTmp)) {
                    Log.i(TAG, "action for client " + socket.hashCode() + ": disconnected");
                    break;
                }
                final String userText = userTextTmp;
                final String message = messageTmp;
                // Send the key command to activities that listen to the service
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                      if (message.equals(Commands.SELECT)) rcs.sendMessageToUI(RemoteControlService.CMD__SELECT, null);
                        else if (message.equals(Commands.MOVE_UP)) rcs.sendMessageToUI(RemoteControlService.CMD__MOVE_UP, null);
                        else if (message.equals(Commands.MOVE_DOWN)) rcs.sendMessageToUI(RemoteControlService.CMD__MOVE_DOWN, null);
                        else if (message.equals(Commands.SOUND_PLUS)) rcs.sendMessageToUI(RemoteControlService.CMD__SOUND_PLUS, null);
                        else if (message.equals(Commands.SOUND_MINUS)) rcs.sendMessageToUI(RemoteControlService.CMD__SOUND_MINUS, null);
                    }
                });
                // update UI
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (!"Test".equals(message)) {
                            rcs.sendMessageToUI(RemoteControlService.MSG__PRINT_NEW_CLIENT_ACTION, message);
                        }
                    }
                });
                // send feedback
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
                Log.e(TAG, e.getMessage());
            }
        }
    }

    public void sendMessage(final String msg) {
        try {
            out.println(msg);
            Log.d(TAG, "send to client " + socket.hashCode() + ": " + msg);
        } catch (Exception e) {
            Log.e(TAG, "ERROR:", e);
        }
    }
}