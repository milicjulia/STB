package com.example.stb.komunikacija;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by vnesic on 19.4.16..
 */
public class BootStart extends BroadcastReceiver {
	String CUSTOM_INTENT = "RESTART_YEAH";
    public void onReceive(Context arg0, Intent arg1)
    {

        arg0.startService(new Intent(arg0, RemoteControlService.class));


        Log.i("Autostart", "started");
        
        Log.d("juliam","BootStarp started");
    }
}