
package com.example.stb.komunikacija;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import com.example.stb.MainActivity;
import java.util.ArrayList;

public class RemoteControlService extends Service {
    public static final String TAG = "RemoteControlService";

    ArrayList<Messenger> mClients = new ArrayList<Messenger>(); // Keeps track of all current registered clients.

    public static final int CMD__MOVE_UP = 16;
    public static final int CMD__MOVE_DOWN = 17;
    public static final int CMD__SOUND_PLUS = 25;
    public static final int CMD__SOUND_MINUS = 26;

    final Messenger mMessenger = new Messenger(new IncomingHandler());

    @Override
    public void onCreate() {
        super.onCreate();
        ServerRunnable server_runnable = new ServerRunnable(this, MainActivity.COMMUNICATION_PORT);
        Thread server_thread = new Thread(server_runnable);
        server_thread.start();
        Log.i(TAG, "Service Started.");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if(mClients.size()==0) mClients.add(msg.replyTo);
            super.handleMessage(msg);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Received start id " + startId + ": " + intent);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Service Stopped.");
    }

    public void sendMessageToUI(int msg_type, String msg_value) {
            try {
                Bundle b = new Bundle();
                b.putString("msg_value", ""+msg_value);
                Message msg = Message.obtain(null, msg_type);
                msg.setData(b);
                mClients.get(0).send(msg);
            } catch (RemoteException e) {
                mClients.remove(0);
            }

    }



}

