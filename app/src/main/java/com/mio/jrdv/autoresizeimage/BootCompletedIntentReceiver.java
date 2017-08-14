package com.mio.jrdv.autoresizeimage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by esq00931 on 31/10/2016.
 * PARA EL AUTOBOOT!!!
 * HAYQ UE AÑADIRLO EN MANIFEST TAMBIEN Y QUE FILTER POR EL INTENT android.intent.action.BOOT_COMPLETED
 */

public class BootCompletedIntentReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            Intent pushIntent = new Intent(context, AutoResizeImageService.class);
           // pushIntent.putExtra(LockService.EXTRA_MESSAGE,"REBOOT");
            //EN VEZ DE ESE EXTRA que sea como si fuera un encender pantalla:
            boolean screenOff=false;//la pantalla com si se cabara de encender!!
            pushIntent.putExtra(AutoResizeImageService.EXTRA_MESSAGE,"screen_state");
            pushIntent.putExtra("screen_state", screenOff);

            context.startService(pushIntent);
        }
    }
}