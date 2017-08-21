package com.mio.jrdv.autoresizeimage;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.amirarcane.recentimages.RecentImages;
import com.amirarcane.recentimages.thumbnailOptions.ImageAdapter;
import com.jess.ui.TwoWayAdapterView;
import com.jess.ui.TwoWayGridView;
import com.muddzdev.styleabletoastlibrary.StyleableToast;

import java.io.File;
import java.io.IOException;

public class Choose2ResizeActivity extends AppCompatActivity {





    private Uri imageUri;
    private ImageView imageView;
    private ContentResolver contentResolver;
    private File photoFile = null;
    private RecentImages recentImages;


    //pra el dobñle tap


    private static final long DOUBLE_CLICK_TIME_DELTA = 300;//milliseconds

    long lastClickTime = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose2_resize);


        //To hide AppBar for fullscreen.
        ActionBar ab = getSupportActionBar();
        ab.hide();


        final TwoWayGridView twoWayGridView = (TwoWayGridView) findViewById(R.id.gridview);

        contentResolver = this.getContentResolver();
        recentImages = new RecentImages();
        recentImages.setHeight(120);
        recentImages.setWidth(120);

        recentImages.setSize(2);//CALIDAD DE 1 A 4 ..MAXIMO=1


        final ImageAdapter adapter = recentImages.getAdapter(Choose2ResizeActivity.this);


        twoWayGridView.setAdapter(adapter);


        /////////////////////SINGLE TAP////////////////////

        twoWayGridView.setOnItemClickListener(new TwoWayAdapterView.OnItemClickListener() {

    public void onItemClick(TwoWayAdapterView parent, View v, int position, long id) {


        long clickTime = System.currentTimeMillis();
        if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA){

            //TODO en lugar de toast usamos https://github.com/Muddz/StyleableToast

            showNewToast("      RESIZING      " );


            imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
            Bitmap bitmap = null;
            Drawable d = null;
            try {
                int orientation = getOrientation(contentResolver, (int) id);
                bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri);
                d = getRotateDrawable(bitmap, orientation);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //  imageView.setImageDrawable(d);


        } else {
           // onSingleClick(v);
        }
        lastClickTime = clickTime;






            }
        });




        //redondeamos el ICONO DE LA GALERIA ELEGIDA:
        //no necesariop se hace fondo trnasparente y punto


        ImageView icon=(ImageView)findViewById(R.id.NOButton);
        Drawable icondraw = null;
        String APKName=Myapplication.preferences.getString(Myapplication.PREF_APKNAMEDELSPINNER,"NONE");

        if (!APKName.equals("NONE")) {

            try {
                icondraw = getPackageManager().getApplicationIcon(APKName);


            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            icon.setImageDrawable(icondraw);

        }

        else
        {



            icon.setImageResource(R.drawable.no_icon);

        }
/*
        //ahopr alo redeondeamos


            Bitmap largeIcon = ((BitmapDrawable)icon.getDrawable()).getBitmap();


            RoundedBitmapDrawable img = RoundedBitmapDrawableFactory.create(getResources(), largeIcon);

//asi con un radio

        img.setCornerRadius(150.0f);

//asi es circular perfecta
//img.setCornerRadius(Math.min(img.getMinimumWidth(), img.getMinimumHeight())/2.0f);

        icon.setImageDrawable(img);

  */



    }


    public void PulsadoNO(View view) {

        finish();
    }

    public void PulsadoYES(View view) {

        //TODO ver que hacer!!!

    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////DETECTATR BACK BUTTON//////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onBackPressed() {
        return;
    }




    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////DOBLE TAP!!!//////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    public abstract class DoubleClickListener implements View.OnClickListener {

        private static final long DOUBLE_CLICK_TIME_DELTA = 300;//milliseconds

        long lastClickTime = 0;

        @Override
        public void onClick(View v) {
            long clickTime = System.currentTimeMillis();
            if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA){
                onDoubleClick(v);
            } else {
                onSingleClick(v);
            }
            lastClickTime = clickTime;
        }

        public abstract void onSingleClick(View v);
        public abstract void onDoubleClick(View v);
    }





////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////METODOS DEL RECENT IMAGES//////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    private Drawable getRotateDrawable(final Bitmap b, final float angle) {
        final BitmapDrawable drawable = new BitmapDrawable(getResources(), b) {
            @Override
            public void draw(final Canvas canvas) {
                canvas.save();
                canvas.rotate(angle, b.getWidth() / 2, b.getHeight() / 2);
                super.draw(canvas);
                canvas.restore();
            }
        };
        return drawable;
    }


    private int getOrientation(ContentResolver cr, int id) {

        String photoID = String.valueOf(id);

        Cursor cursor = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media.ORIENTATION}, MediaStore.Images.Media._ID + "=?",
                new String[]{"" + photoID}, null);
        int orientation = -1;

        if (cursor.getCount() != 1) {
            return -1;
        }

        if (cursor.moveToFirst()) {
            orientation = cursor.getInt(0);
        }
        cursor.close();
        return orientation;
    }

    @Override
    protected void onDestroy() {
        recentImages.cleanupCache();
        super.onDestroy();
    }


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////METODOS DEL TOAST//////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    private void showNewToast(String texto2Toast) {


        StyleableToast st = new StyleableToast(Choose2ResizeActivity.this, texto2Toast, Toast.LENGTH_SHORT);
        st.setBackgroundColor(Color.parseColor("#ff5a5f"));
        st.setTextColor(Color.WHITE);
        st.setIcon(R.mipmap.ic_launcher);//TODO poner icono app
        st.spinIcon();
        st.setCornerRadius(20);
        st.setMaxAlpha();
        st.show();



    }
}
