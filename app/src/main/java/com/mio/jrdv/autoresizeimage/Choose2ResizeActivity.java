package com.mio.jrdv.autoresizeimage;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class Choose2ResizeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose2_resize);


        //To hide AppBar for fullscreen.
        ActionBar ab = getSupportActionBar();
        ab.hide();
    }
}
