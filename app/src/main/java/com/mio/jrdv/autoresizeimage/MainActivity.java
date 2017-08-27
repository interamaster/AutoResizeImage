package com.mio.jrdv.autoresizeimage;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.admin.DevicePolicyManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.muddzdev.styleabletoastlibrary.StyleableToast;

import java.util.ArrayList;
import java.util.List;



//V02 añadido icono de apk galeria y doble tap detectado con toast
//v03 añadido resize de images que guarda en IMAGENES con animacion y toast
//v032 añadido pref de nombre app maldita ok en service para que lo sepa desde el boot ycambiado screenreceiver a solo reiniciar service al encender pantalla
//v033 cambiado chequeo de abrir o no service activity
//v07 añadido panel de instruuciones al inicio..versio 0.5
//v075 añadido panel de ins en Choosetresizeactivity
//v095 añadido animacion y botn de ayuda, anuncios creados flata solo  icono de apk y quitar LOGS
//v1 final en google play iconos ok y quitaods LOG
//v1,01 añadido español y cambiado back button ahor asigue a gasleria

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

        //usageAccessSettingsPage();

        //metodo2

        //needPermissionForBlocking(this);


        //metodo 3

      if (!isAccessGranted()) {
           // Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
           // startActivity(intent);



        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Usage Access")
                .setMessage("App will not run without usage access permissions.")
                .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                        // intent.setComponent(new ComponentName("com.android.settings","com.android.settings.Settings$SecuritySettingsActivity"));
                       // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//TENGO QUE QUIATRLO O onActivityResult SE EJECUTA ANTES
                        startActivityForResult(intent,REQUEST_CODE);//oara e activityresult es request code =0!!!
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .create();


        alertDialog.show();

      }



    //TODO habilitar ADMIN

       // EnableAdmin();



        Util.checkPermission(
                this,
                new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA
                },
                new Util.OnPermissionCallback() {
                    @Override
                    public void onPermissionGranted() {
                    }

                    @Override
                    public void onPermissionDenied() {
                        finish();
                    }
                }
        );



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

       //para que ponga el valor ya elegido en ele spinner
        String GaleriaElegidaName = Myapplication.preferences.getString(Myapplication.PREF_APKNAMEDELSPINNER,"NO");//the value you want the position for

        if (!GaleriaElegidaName.equals("NO")) {

            ArrayAdapter myAdap = (ArrayAdapter) spApksParaGaleria.getAdapter();
            int spinnerPosition = myAdap.getPosition(GaleriaElegidaName);

            //set the default according to value
            spApksParaGaleria.setSelection(spinnerPosition);

        }


        //para elegir del spinner


        spApksParaGaleria.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapter, View v,
                                       int position, long id) {
                // On selecting a spinner item
                APKNAMEDELSPINNER = adapter.getItemAtPosition(position).toString();
                // Showing selected spinner item
               // Toast.makeText(getApplicationContext(),  "Selected Galeria : " + APKNAMEDELSPINNER, Toast.LENGTH_SHORT).show();

                showNewToast("Selected Image Aplication: "+APKNAMEDELSPINNER);

               // Log.e("INFO TOAST","Selected Galeria : " + APKNAMEDELSPINNER);

                AutoResizeImageService.PACKAGEMALDITO1=APKNAMEDELSPINNER;

                //lo guardamos en pref

                Myapplication.preferences.edit().putString(Myapplication.PREF_APKNAMEDELSPINNER,APKNAMEDELSPINNER).commit();

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



    private boolean isAccessGranted() {
        try {
            PackageManager packageManager = getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(getPackageName(), 0);
            AppOpsManager appOpsManager = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
            int mode = 0;
            if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.KITKAT) {
                mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                        applicationInfo.uid, applicationInfo.packageName);
            }
            return (mode == AppOpsManager.MODE_ALLOWED);

        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //el reqquestCode es 0(el que llmamamos desde el intent!!!



        if(REQUEST_CODE == requestCode)
        {

            //volvemos a chequear que se activo:

          //  Log.d("info", "onactivityresult chequeamos si se habilito ya el usagestats  requestcode:"+requestCode);




            if (!isAccessGranted()) {
                // Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                // startActivity(intent);

              //  Log.d("info", "onactivityresult chequeamos si se habilito ya el usagestats ");



                //TODO en lugar de toast usamos https://github.com/Muddz/StyleableToast

                showNewToast("YOU HAVE TO ENABLE ME TO WORK!!!!" );


            }

            else {
                //si se habilito..empieza el service

              //no hacemos nada
            }

        }

    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////NEW TOAST//////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////




    private void showNewToast(String texto2Toast) {


        StyleableToast st = new StyleableToast(MainActivity.this, texto2Toast, Toast.LENGTH_SHORT);
        //st.setBackgroundColor(Color.parseColor("#ff5a5f"));
        st.setBackgroundColor(Color.LTGRAY);
        st.setTextColor(Color.WHITE);
        st.setIcon(R.mipmap.ic_launcher);//TODO poner icono app
        st.spinIcon();
        st.setCornerRadius(20);
        st.setMaxAlpha();
        st.show();



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


           // Log.d("INFO",infos.activityInfo.processName);
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


            //Log.d("INFO EN IMAGECHOOSE",infos.activityInfo.processName);
            //return infos.activityInfo.processName;
            apks.add(infos.activityInfo.processName);

            //return apks;






        }

        return apks;
        //return null;
    }


    public void Salir(View view) {

        //start AutoResizeImageService

       // StartServiceYa();


        if (!isAccessGranted()) {
            // Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            // startActivity(intent);

           // Log.d("info", "onactivityresult chequeamos si se habilito ya el usagestats ");



            //TODO en lugar de toast usamos https://github.com/Muddz/StyleableToast

            showNewToast("YOU HAVE TO ENABLE ME TO WORK!!!!" );


            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setTitle("WARNING!!!!")
                    .setMessage("App will not run without usage access permissions.")
                    .setPositiveButton("DONT CARE", new DialogInterface.OnClickListener() {
                        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                        public void onClick(DialogInterface dialog, int which) {

                            if (!isMyServiceRunning(AutoResizeImageService.class)){
                                StartServiceYa();
                            }
                           finish();
                            }
                    })
                    .setNegativeButton("OK LETS DO IT", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                            dialog.dismiss();

                            // continue with delete
                            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                            // intent.setComponent(new ComponentName("com.android.settings","com.android.settings.Settings$SecuritySettingsActivity"));
                            //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//TENGO QUE QUIATRLO O onActivityResult SE EJECUTA ANTES
                            startActivityForResult(intent,REQUEST_CODE);//oara e activityresult es request code =0!!!
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .create();


            alertDialog.show();


        }





        else {


            if (!isMyServiceRunning(AutoResizeImageService.class)){
                StartServiceYa();
            }
            else {
                finish();
            }

        }
    }
}
