package com.example.stb;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.example.stb.servis.RemoteControlService;

public class STBRemoteControlCommunication {

    private static final String TAG = "STBRemoteControlCom";

    Messenger mService = null;
    boolean mIsBound;
    final Messenger mMessenger = new Messenger(new IncomingHandler());
    private static final int MSG__REGISTER_CLIENT = 1;
    private static final int MSG__UNREGISTER_CLIENT = 2;
    public static final int CMD__MOVE_UP = 16;
    public static final int CMD__MOVE_DOWN = 17;
    public static final int CMD__SELECT = 20;
    public static final int CMD__SOUND_PLUS = 25;
    public static final int CMD__SOUND_MINUS = 26;

    private MainActivity act;
    private boolean[] escape = {false, false};

    public STBRemoteControlCommunication(MainActivity a) {
        act = a;
    }

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "Message: " + msg.what);
            Log.d(TAG, "escape: " + escape[0] + ", " + escape[1]);
            if (msg.what == CMD__MOVE_DOWN) {
                escape[0] = true;
                Log.d(TAG, "escape1");
            } else if (msg.what == CMD__MOVE_UP && escape[0]) {
                escape[1] = true;
                Log.d(TAG, "escape2");
            }else if (msg.what != RemoteControlService.MSG__PRINT_NEW_CLIENT_ACTION) {
                escape[0] = false;
                escape[1] = false;
            }
            switch (msg.what) {
                case RemoteControlService.MSG__PRINT_NEW_CLIENT:
                    String client = msg.getData().getString("msg_value");
                    act.add_client(client);
                    break;
                case RemoteControlService.MSG__PRINT_NEW_CLIENT_ACTION:
                    String client_action = msg.getData().getString("msg_value");
                    act.add_client_action(client_action);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }


    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            try {
                Message msg = Message.obtain(null, MSG__REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mService.send(msg);
            } catch (RemoteException e) {
                Log.e(TAG, "error :\n", e);
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            mService = null;
        }
    };

    public void doBindService() {
        act.bindService(new Intent("RemoteControlService.intent.action.Launch"), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    public void doUnbindService() {
        if (mIsBound) {
            if (mService != null) {
                try {
                    Message msg = Message.obtain(null, MSG__UNREGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                } catch (RemoteException e) {
                    Log.e("TAG", "error:\n", e);
                }
            }
            act.unbindService(mConnection);
            mIsBound = false;
        }
    }
}