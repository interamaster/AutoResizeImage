package com.mio.jrdv.autoresizeimage;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by joseramondelgado on 02/11/16.
 * necesario para el DEVICE ADMIN
 */

public class DeviceAdmin extends DeviceAdminReceiver
{
    // implement onEnabled(), onDisabled(),
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
    }

    public void onEnabled(Context context, Intent intent) {};

    public void onDisabled(Context context, Intent intent) {};
}