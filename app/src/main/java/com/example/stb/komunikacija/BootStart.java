package com.example.stb.komunikacija;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.app.Service;
import android.os.Build;
import android.util.Log;

/**
 * Created by vnesic on 19.4.16..
 */
public class BootStart extends BroadcastReceiver {
    public void onReceive(Context arg0, Intent arg1) {
        if (!Build.MODEL.equals("moto e5")) {

        Intent intent = new Intent(arg0, RemoteControlService.class);
        intent.putExtra("kind", 10);
        intent.putExtra("first_start",true);
        arg0.startService(intent);
        Log.i("Autostart", "started");
    }
    }
}