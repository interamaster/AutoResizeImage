package com.mio.jrdv.autoresizeimage;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class Choose2ResizeActivity extends AppCompatActivity {

    ArrayAdapter<String> AppQueAbrenGaleriaArray;
    Spinner spApksParaGaleria;
    String APKNAMEDELSPINNER;

    List<String> apks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose2_resize);


        //To hide AppBar for fullscreen.
        ActionBar ab = getSupportActionBar();
        ab.hide();





        /////////////////////spinner////////////////////////////

        spApksParaGaleria = (Spinner) findViewById(R.id.spApsQueAbrenGaleria);

        //lamamos funcion para llenar Listde nonmbres de apks que abren galeria

        getPackageForGalery();

        // Initialize and set Adapter del SPinner
        AppQueAbrenGaleriaArray = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, apks);


        AppQueAbrenGaleriaArray.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spApksParaGaleria.setAdapter(AppQueAbrenGaleriaArray);


        spApksParaGaleria.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapter, View v,
                                       int position, long id) {
                // On selecting a spinner item
                APKNAMEDELSPINNER = adapter.getItemAtPosition(position).toString();
                // Showing selected spinner item
                Toast.makeText(getApplicationContext(),
                        "Selected Galeria : " + APKNAMEDELSPINNER, Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });
    }



    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////SABER LAS APKS QUE PUEDEN ABRIR GALERIA//////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    public List<String> getPackageForGalery() {
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
}
