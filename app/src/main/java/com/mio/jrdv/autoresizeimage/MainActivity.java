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
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    //para el device manager

    private static final int REQUEST_CODE = 0;
    private DevicePolicyManager mDPM;
    private ComponentName mAdminName;


    //para spinner

    ArrayAdapter<String> AppQueAbrenGaleriaArray;
    Spinner spApksParaGaleria;
    String APKNAMEDELSPINNER;

    List<String> apks;




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



/*

//TODO volver a pobner tas quitar spinner

        //CHEECK APKS QUE ABREN GALERIA
        getPackageForGalery();

        //start AutoResizeImageService

        StartServiceYa();

  */

        ////////TODO quitar SPINNER//////////////////////////////



        ///////////////////custom sinner con imagenes y tecxto:


        spApksParaGaleria = (Spinner) findViewById(R.id.spApsQueAbrenGaleria);

        //llamamos funcion para llenar Listde nonmbres de apks que abren galeria

        getPackageForGalery2();

        //Spinner mySpinner = (Spinner)findViewById(R.id.spinner);
        spApksParaGaleria.setAdapter(new MyAdapter(MainActivity.this, R.layout.row, apks));



        spApksParaGaleria.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapter, View v,
                                       int position, long id) {
                // On selecting a spinner item
                APKNAMEDELSPINNER = adapter.getItemAtPosition(position).toString();
                // Showing selected spinner item
                Toast.makeText(getApplicationContext(),
                        "Selected Galeria : " + APKNAMEDELSPINNER, Toast.LENGTH_SHORT).show();

                Log.e("INFO TOAST","Selected Galeria : " + APKNAMEDELSPINNER);

                AutoResizeImageService.PACKAGEMALDITO1=APKNAMEDELSPINNER;
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });

    }


    public class MyAdapter extends ArrayAdapter{
        public MyAdapter(Context context, int textViewResourceId, List<String> objects) {
            super(context, textViewResourceId, objects);
        }
        @Override
        public View getDropDownView(int position, View convertView,ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }
        public View getCustomView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater=getLayoutInflater();
            View row=inflater.inflate(R.layout.row, parent, false);
            TextView label=(TextView)row.findViewById(R.id.app_name);
            label.setText(apks.get(position));


            //el icono desed el packagename:
            //Drawable icon = getPackageManager().getApplicationIcon("com.example.testnotification");
            //imageView.setImageDrawable(icon);
            ImageView icon=(ImageView)row.findViewById(R.id.app_icon);
            Drawable icondraw = null;
            try {
                icondraw = getPackageManager().getApplicationIcon(apks.get(position));
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            icon.setImageDrawable( icondraw);
            return row;
        }



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




//////////////////////////////////////////IDEM DEVOLVIENDO ARAY CON NOMBRES/////////////////////////////////////////



    public List<String> getPackageForGalery2() {
        Intent mainIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        mainIntent.setType("image/*");
        List<ResolveInfo> pkgAppsList = getApplicationContext().getPackageManager().queryIntentActivities(mainIntent, PackageManager.GET_RESOLVED_FILTER);
        int size = pkgAppsList.size();

        apks = new ArrayList<String>();

        for (ResolveInfo infos : pkgAppsList) {


            Log.d("INFO EN IMAGECHOOSE",infos.activityInfo.processName);
            //return infos.activityInfo.processName;
            apks.add(infos.activityInfo.processName);

            //return apks;






        }

        return apks;
        //return null;
    }


    public void Salir(View view) {

        //start AutoResizeImageService

        StartServiceYa();
    }
}
