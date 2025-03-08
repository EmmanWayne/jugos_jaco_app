package com.jugos_jaco_app.ui.utilities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class LocationServiceRestartReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && 
            (intent.getAction().equals("android.intent.action.BOOT_COMPLETED") ||
             intent.getAction().equals("android.intent.action.QUICKBOOT_POWERON"))) {
            
            Intent serviceIntent = new Intent(context, LocationService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
            } else {
                context.startService(serviceIntent);
            }
        }
    }
} 