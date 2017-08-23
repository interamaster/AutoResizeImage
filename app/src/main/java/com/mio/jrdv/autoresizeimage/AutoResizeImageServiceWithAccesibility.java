package com.mio.jrdv.autoresizeimage;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import java.util.concurrent.ScheduledExecutorService;

public class AutoResizeImageServiceWithAccesibility extends AccessibilityService {


    //TODO NO NECESITO QUE SEA UN ACCESIBIITLYT


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



    public AutoResizeImageServiceWithAccesibility() {
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {

        Log.d("INFO","ACCESIBILITY EVENT DETECTED!!! IN  AutoResizeImage NEW");

    }

    @Override
    public void onInterrupt() {
        Log.d("INFO","INTERRUMPIDO AutoResizeImage NEW");

    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d("TAG", "onServiceConnected");
        Log.d("INFO","ACCESIBILITY EVENT DETECTED!!! IN  AutoResizeImage NEW");






        //volvemos a Mainactivity

        Intent startIntent = new Intent(this, MainActivity.class);
        startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(startIntent);




    }



}
