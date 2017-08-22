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
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amirarcane.recentimages.RecentImages;
import com.amirarcane.recentimages.thumbnailOptions.ImageAdapter;
import com.github.glomadrian.grav.GravView;
import com.jess.ui.TwoWayAdapterView;
import com.jess.ui.TwoWayGridView;
import com.muddzdev.styleabletoastlibrary.StyleableToast;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

import id.zelory.compressor.Compressor;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class Choose2ResizeActivity extends AppCompatActivity {





    private Uri imageUri;
    private ImageView imageView;
    private ContentResolver contentResolver;
    private File photoFile = null;
    private RecentImages recentImages;

    private TextView imageSizeText;

    //pra el dobñle tap


    private static final long DOUBLE_CLICK_TIME_DELTA = 300;//milliseconds

    long lastClickTime = 0;

    //para el tamalo imagen

    private File actualImage;
    private File compressedImage;

    //para la animacio  de fondo

    GravView aniamtionFondo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose2_resize);


        //To hide AppBar for fullscreen.
        ActionBar ab = getSupportActionBar();
        ab.hide();

        imageSizeText=(TextView)findViewById(R.id.TEXTinfoSize);

        aniamtionFondo=(GravView)findViewById(R.id.grav2);


        final TwoWayGridView twoWayGridView = (TwoWayGridView) findViewById(R.id.gridview);

        contentResolver = this.getContentResolver();
        recentImages = new RecentImages();
        recentImages.setHeight(120);
        recentImages.setWidth(120);

        recentImages.setSize(2);//CALIDAD DE 1 A 4 ..MAXIMO=1


        final ImageAdapter adapter = recentImages.getAdapter(Choose2ResizeActivity.this);


        twoWayGridView.setAdapter(adapter);


        /////////////////////DOBLE TAP////////////////////

        twoWayGridView.setOnItemClickListener(new TwoWayAdapterView.OnItemClickListener() {

    public void onItemClick(TwoWayAdapterView parent, View v, int position, long id) {



        imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);



        try {
            actualImage = FileUtil.from(Choose2ResizeActivity.this,imageUri);
        } catch (IOException e) {
            e.printStackTrace();
        }


        long clickTime = System.currentTimeMillis();
        if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA){



            //mostramos la animacion de fondo

            aniamtionFondo.setVisibility(View.VISIBLE);

            //creamos una thread de 1 segundo de animacion de fondo

            Thread thread = new Thread(){
                @Override
                public void run() {
                    try {
                        Thread.sleep(2000); // As I am using LENGTH_LONG in Toast

                        //mostramos la animacion de fondo

                        aniamtionFondo.setVisibility(View.INVISIBLE);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };


            //empezamos el thread

            thread.start();


            //TODO en lugar de toast usamos https://github.com/Muddz/StyleableToast

            showNewToast("      RESIZING      " );


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


            //AQUI COMPRIMIMOS




            // Compress image using RxJava in background thread with custom Compressor
            new Compressor(Choose2ResizeActivity.this)
                    .setMaxWidth(640)
                    .setMaxHeight(480)
                    .setQuality(75)
                    .setCompressFormat(Bitmap.CompressFormat.WEBP)
                    .setDestinationDirectoryPath(Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_PICTURES).getAbsolutePath())
                    .compressToFileAsFlowable(actualImage)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<File>() {
                        @Override
                        public void accept(File file) {
                            compressedImage = file;
                            setCompressedImage();
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) {
                            throwable.printStackTrace();
                            showError(throwable.getMessage());
                        }
                    });



        } else {
           // onSingleClick(v);

            //vamos a mostrar lo que ocupa:


            imageSizeText.setText(String.format("IMAGE SIZE:  %s", getReadableFileSize(actualImage.length())));

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


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////METODOS compressor saber tamaño//////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////



    public String getReadableFileSize(long size) {
        if (size <= 0) {
            return "0";
        }
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }




    public void showError(String errorMessage) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }



    private void setCompressedImage() {





        //TODO animacion o algo




        Toast.makeText(this, "Compressed image save in " + compressedImage.getPath(), Toast.LENGTH_LONG).show();
        Log.d("Compressor", "Compressed image save in " + compressedImage.getPath());
    }

}
