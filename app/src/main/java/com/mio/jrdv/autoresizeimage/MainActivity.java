package com.mio.jrdv.autoresizeimage;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    //para el device manager

    private static final int REQUEST_CODE = 0;
    private DevicePolicyManager mDPM;
    private ComponentName mAdminName;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //To hide AppBar for fullscreen.
        ActionBar ab = getSupportActionBar();
        ab.hide();


        //habilitamos el usagestats
        //metodo1

        usageAccessSettingsPage();

        //metodo2

        //needPermissionForBlocking(this);


        //TODO habilitar ADMIN

        //EnableAdmin();




        //CHEECK APKS QUE ABREN GALERIA
        getPackageForGalery();

        //start AutoResizeImageService

        StartServiceYa();

    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////saber si mi service esat runnig/////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    private void StartServiceYa() {

        Intent intent =new Intent(this,AutoResizeImageService.class);
        intent.putExtra(AutoResizeImageService.EXTRA_MESSAGE,"DesdeMain");

        startService(intent);

        finish();
    }




    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////device manager PTE de HACER O NO..//////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////



    private void EnableAdmin() {



        try
        {
            // Initiate DevicePolicyManager.
            mDPM = (DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE);
            // Set DeviceAdminDemo Receiver for active the component with different option
            mAdminName = new ComponentName(this, DeviceAdmin.class);

            if (!mDPM.isAdminActive(mAdminName)) {
                // try to become active
                Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mAdminName);
                intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Pulsa activar app!!");
                startActivityForResult(intent, REQUEST_CODE);
            }
            else
            {
                // Already is a device administrator, can do security operations now.
                //TODO asi se puede bloquear!!! : mDPM.lockNow();
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////SABER EL PACKAGE NAME DE LA APP QUE ESTA ACTIVA NECESIAT PERMISO EXTRAS FUNCIONA EN TODOS/////////////
    ///////////////////////////////////INCLUSO EN NOUGAT///////////////////////////////////////////////////////////



    public void usageAccessSettingsPage(){
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromParts("package", this.getPackageName(), null);
        intent.setData(uri);

        if(intent.resolveActivity(getPackageManager()) != null) {


            //startActivityForResult(intent, 0);
            startActivity(intent);
        }

        else{

            //TODO
        }


    }


    public static boolean needPermissionForBlocking(Context context){
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
            AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            int mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, applicationInfo.uid, applicationInfo.packageName);
            return  (mode != AppOpsManager.MODE_ALLOWED);
        } catch (PackageManager.NameNotFoundException e) {
            return true;
        }
    }



    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////SABER LAS APKS QUE PUEDEN ABRIR GALERIA//////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    public String getPackageForGalery() {
        Intent mainIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        mainIntent.setType("image/*");
        List<ResolveInfo> pkgAppsList = getApplicationContext().getPackageManager().queryIntentActivities(mainIntent, PackageManager.GET_RESOLVED_FILTER);
        int size = pkgAppsList.size();
        for (ResolveInfo infos : pkgAppsList) {


            Log.d("INFO",infos.activityInfo.processName);
            return infos.activityInfo.processName;



        }
        return null;
    }

}
