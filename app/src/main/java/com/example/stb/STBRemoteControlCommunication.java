package com.example.stb;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

public class STBRemoteControlCommunication {

    private static final String TAG = "juliam";

    Messenger mService = null;
    boolean mIsBound;

    private MainActivity act;
    private boolean[] escape = {false, false};

    public STBRemoteControlCommunication (MainActivity a) {
        act = a;
    }

    class IncomingHandler extends Handler {
        @SuppressLint("LongLogTag")
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "Message: " + msg.what);
            Log.d(TAG, "escape: " + escape[0] + ", " + escape[1]);
           super.handleMessage(msg);

        }
    }


    private ServiceConnection mConnection = new ServiceConnection() {
        @SuppressLint("LongLogTag")
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
        }

        public void onServiceDisconnected(ComponentName className) {
            mService = null;
        }
    };

    public void doBindService() {
        Log.d("juliam","trying to connect");
        Intent intent = new Intent("com.example.stb.komunikacija.RemoteControlService");
        intent.setPackage("com.example.stb.komunikacija");
        act.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        //act.bindService(new Intent("komunikacija.RemoteControlService.intent.action.Launch"), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
        Log.d("juliam","connected");
    }

    @SuppressLint("LongLogTag")
    public void doUnbindService() {
        if (mIsBound) {
            act.unbindService(mConnection);
            mIsBound = false;
        }
    }
}