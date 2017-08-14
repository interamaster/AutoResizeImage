package com.mio.jrdv.autoresizeimage;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import java.util.Calendar;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AutoResizeImageService extends Service {

    public static AutoResizeImageService instance;

    //para la repeticion cada seg

    ScheduledExecutorService scheduler;
    //para saber si ya esta el seviCio running:
    private boolean ServiceYaRunning;

    //CONTEXT
    private Context mContext;
    //para el intnt Extra info

    public static final String  EXTRA_MESSAGE="mensaje";


    @Override
    public void onCreate() {
        super.onCreate();
        ServiceYaRunning=false;

        Log.d("INFO","INICIADO onCreate EN AUTORESIZEIMAGESERVICE!!");

        mContext = this;

        // REGISTER RECEIVER THAT HANDLES SCREEN ON AND SCREEN OFF LOGIC
        //NO CREO Q SEA NECESARIO LA TENRELO EN MANIFEST!!!NO!!! SI LO QUITO NO FUNCIONA!!

        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        BroadcastReceiver mReceiver = new ScreenReceiver();
        registerReceiver(mReceiver, filter);

    }



////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////METODO QUE SE EJECUTA CADA VEZ QUE SE RELANZA ESTE SERVICE//////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO OJO ESTE METODO SE EJECUTA CADA VEZ QUE SE LANZA UN INTENT DE ESTE SERVICE
        //SI YA ESTABA CREADO!!!
        //ASI QUE ES LA MEJO MANERA DE ACTUALIZAR LA INFO!!

        //ej leer el extra del intent:


        Log.d("INFO", "REINICIADO onStartCommand EN AUTORESIZEIMAGESERVICE!!");


        //inicamos la repeticion
        //NO !!!!!!:lo pongo en oncreate o se para y arranca cada vez que hay un nuevo intent(por ej apaagar pantalla)
        //si lo pongo en oncreate no empieza!!!
        scheduleMethod();


        return Service.START_STICKY;



    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////SCHEDULE METHOD QEU SE REPITE CADA SEGUNDO//////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void scheduleMethod() {


        //ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        //creamos una property emjor para deetctar si ya empezo o no
        //PERO EL PROBLEMA REAL ES  QUE LOS Executors SE PARA CUANDO LA CPU SE PONE EN REPOSO(EJ APGARA PANTALLA!!)


        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {

                //se supone que ambos funionana hasta nougat incluido!!
                gettopactivity();//con este hacen falta permisos
                printForegroundTask();//y con este tambien


            }
        }, 0, 1000, TimeUnit.MILLISECONDS);


    }


    public AutoResizeImageService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }





/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////METODO 1 SABER APP FOREGROUND INCLUIDO NOUGAT..ESTE USADO POR MI EN KIDSTIMER//////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////



    public void gettopactivity() {

        //NECESITA PERMISOS !!!

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            UsageStatsManager usm = (UsageStatsManager) getSystemService("usagestats");
            long time = System.currentTimeMillis();
            List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,
                    time - 1000 * 1000, time);
            if (appList != null && appList.size() > 0) {
                SortedMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
                for (UsageStats usageStats : appList) {
                    mySortedMap.put(usageStats.getLastTimeUsed(),
                            usageStats);
                }
                if (mySortedMap != null && !mySortedMap.isEmpty()) {
                    String currentApp = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                    //Log.v("INFO currentapp: ", currentApp);
                }
            }
        } else {
            ActivityManager am = (ActivityManager) getBaseContext().getSystemService(ACTIVITY_SERVICE);
            String currentApp = am.getRunningTasks(1).get(0).topActivity.getPackageName();
            //Log.v("INFO currentapp: ", currentApp);

        }
    }


/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////METODO 2 SABER APP FOREGROUND INCLUIDO NOUGAT//////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    private String printForegroundTask() {
        String currentApp = "NULL";
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            UsageStatsManager usm = (UsageStatsManager)this.getSystemService("usagestats");
            long time = System.currentTimeMillis();
            List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,  time - 1000*1000, time);
            if (appList != null && appList.size() > 0) {
                SortedMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
                for (UsageStats usageStats : appList) {
                    mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                }
                if (mySortedMap != null && !mySortedMap.isEmpty()) {
                    currentApp = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                }
            }
        } else {
            ActivityManager am = (ActivityManager)this.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> tasks = am.getRunningAppProcesses();
            currentApp = tasks.get(0).processName;
        }

        Log.e("adapter", "Current App in foreground is: " + currentApp);
        return currentApp;
    }



    @Override
    public void onDestroy() {


         Log.i("INFO", "Proceso  cancelled");
        super.onDestroy();


        //para evitar que el user pueda para el proceso:
        //http://stackoverflow.com/questions/21550204/how-to-automatically-restart-a-service-even-if-user-force-close-it


        //nunca se destruye!!!

    }




    public static void stop() {
        if (instance != null) {
            instance.stopSelf();

             Log.v("INFO  ",  "proceso parado!!!");


        }
    }





}
