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
import java.util.StringTokenizer;
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

    //intent
    public static final String  EXTRA_MESSAGE="mensaje";

    //nombre packages
     private String CURRENT_PACKAGE_NAME ="com.mio.jrdv.autoresizeimage";
    // public  static String PACKAGEMALDITO1="com.android.gallery";//el home screen de LL

    public  static String PACKAGEMALDITO1;
     private String lastAppPN = "";
     private String currenApp="";


    //PARA SABER SI ES LA PRIMERA VEZ QUE ENTRA EN GALERIA
    public boolean FirstTimeAsked2Resize=false;

    //PARA SABER SI DECIDIO NO HACER RESIZE

    public boolean ResizeSelected=false;
    @Override
    public void onCreate() {
        super.onCreate();
        ServiceYaRunning=false;


        //lo guardamos en pref

        PACKAGEMALDITO1=Myapplication.preferences.getString(Myapplication.PREF_APKNAMEDELSPINNER,"NONE");

        Log.d("INFO","INICIADO onCreate EN AUTORESIZEIMAGESERVICE!!");

        Log.d("INFO","INICIADO onCreate con PACKAGEMALDITO1="+PACKAGEMALDITO1);
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
              //  printForegroundTask();//y con este tambien


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
                    currenApp = mySortedMap.get(mySortedMap.lastKey()).getPackageName();

                    Log.v("INFO currentapp: ", currenApp);
                    Log.v("INFO lastapp: ", lastAppPN+"ultima");
                }
            }
        } else {
            ActivityManager am = (ActivityManager) getBaseContext().getSystemService(ACTIVITY_SERVICE);
            currenApp = am.getRunningTasks(1).get(0).topActivity.getPackageName();
         //   lastAppPN = am.getRunningTasks(1).get(1).topActivity.getPackageName();
           // Log.v("INFO currentapp: ", currenApp);
          //  Log.v("INFO lastapp: ", lastAppPN);

        }


        // Provide the packagename(s) of apps here, you want to show password activity
        if (currenApp.equals(PACKAGEMALDITO1) && !FirstTimeAsked2Resize &&!lastAppPN.equals(PACKAGEMALDITO1)) {
          // if (!(lastAppPN.equals(currenApp))) {
                lastAppPN = currenApp;
               Log.e("INFO", "gallery started");
                //es la primera vez q entramos en la galeria
                FirstTimeAsked2Resize=true;

                //arranacamos ACTIVITY para que ELIJA si o no a resize

                Intent lockIntent = new Intent(mContext, Choose2ResizeActivity.class);
                lockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        /*
        public static final int FLAG_ACTIVITY_SINGLE_TOP = 536870912
        If set, the activity will not be launched if it is already running at the top of the history stack.
         */
                //http://stackoverflow.com/questions/8077728/how-to-prevent-the-activity-from-loading-twice-on-pressing-the-button
                //lockIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                //asi se esat simepre ejecuatnfo el onresume y onstop del loginpad!!!lo dejamos como eataba..no es de esto

                mContext.startActivity(lockIntent);

            // }
        }
        else   if (!currenApp.equals(CURRENT_PACKAGE_NAME) && FirstTimeAsked2Resize && lastAppPN.equals(PACKAGEMALDITO1)) {
              //  if (!(currenApp.equals(lastAppPN))) {
                   Log.e("INFO", " gallery stoped");
                   lastAppPN = currenApp;

                    //salimos de la galeria
                    //TODO dependiendo de donde salga sera false o no...
                    FirstTimeAsked2Resize=false;
               // }
                 }

      else   if (!currenApp.contains(CURRENT_PACKAGE_NAME)&& !FirstTimeAsked2Resize && lastAppPN.equals(PACKAGEMALDITO1)){

             Log.e("INFO", "NO ESTAMOS");
            lastAppPN = currenApp;

            FirstTimeAsked2Resize=false;

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



/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////METODO 3 SABER APP FOREGROUND INCLUIDO NOUGAT//////////////////////////////////////////////////////
    ////////////////////////////////////////SE SUPONE MAS LIVIANO Y RAPIDO/////NO FUNCIONA/////////////////////////////////////////////////////////////


    public void gettopactivity2() {


        String topPackageName;
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            UsageStatsManager mUsageStatsManager = (UsageStatsManager) getSystemService("usagestats");
            long currentTime = System.currentTimeMillis();
            // get usage stats for the last 1 seconds
            List<UsageStats> stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, currentTime - 1000 * 1, currentTime);
            // search for app with most recent last used time
            if (stats != null) {
                long lastUsedAppTime = 0;
                for (UsageStats usageStats : stats) {
                    if (usageStats.getLastTimeUsed() > lastUsedAppTime) {
                        topPackageName = usageStats.getPackageName();
                        lastUsedAppTime = usageStats.getLastTimeUsed();

                          Log.v("INFO currentapp: ", topPackageName);
                          // Log.v("INFO lastapp: ", lastUsedAppTime);

                    }
                }
            }
        }
        else {
            ActivityManager am = (ActivityManager) getBaseContext().getSystemService(ACTIVITY_SERVICE);
            currenApp = am.getRunningTasks(1).get(0).topActivity.getPackageName();
            //   lastAppPN = am.getRunningTasks(1).get(1).topActivity.getPackageName();
            // Log.v("INFO currentapp: ", currenApp);
            //  Log.v("INFO lastapp: ", lastAppPN);

        }

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
