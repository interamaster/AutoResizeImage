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
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amirarcane.recentimages.RecentImages;
import com.amirarcane.recentimages.thumbnailOptions.ImageAdapter;
import com.github.glomadrian.grav.GravView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
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

    //para ekl anuancio

    private AdView mAdView;

    //para a ayuda

    TextView TEXTinfo1,TEXTinfo2,TEXTinfo3;
    ImageView FlechaIamgeview;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose2_resize);


        //To hide AppBar for fullscreen.
        ActionBar ab = getSupportActionBar();
        ab.hide();


       // textview de info
        TEXTinfo1=(TextView)findViewById(R.id.TEXTinfo1);
        TEXTinfo2=(TextView)findViewById(R.id.TEXTinfo2);
        TEXTinfo3=(TextView)findViewById(R.id.TEXTinfo3);

        FlechaIamgeview=(ImageView)findViewById(R.id.flechaView) ;

        //al iniciar son invisibles

        TEXTinfo1.setVisibility(View.INVISIBLE);
        TEXTinfo2.setVisibility(View.INVISIBLE);
        TEXTinfo3.setVisibility(View.INVISIBLE);
        FlechaIamgeview.setVisibility(View.INVISIBLE);

        //ads initialize:

        // Initialize the Mobile Ads SDK.
        MobileAds.initialize(this, "ca-app-pub-6700746515260621~88555559614");//ads id de la app!!


        mAdView = (AdView) findViewById(R.id.adView);
        //TODO poner para modo final:
         AdRequest adRequest = new AdRequest.Builder().build();
        //para probar:
/*
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                // Check the LogCat to get your test device ID
                // .addTestDevice("5B700828CEDE278B71E610C31C1E433E")
                .build();
*/

        mAdView.loadAd(adRequest);


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


                       // aniamtionFondo.setVisibility(View.GONE);
                         aniamtionFondo.setVisibility(View.INVISIBLE);
                        //aniamtionFondo.invalidate();
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

        showNewToast(getString(R.string.BACKBUTTON));

       // return;

        finish();
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




////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////METODOS DEL TOAST//////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    private void showNewToast(String texto2Toast) {


        StyleableToast st = new StyleableToast(Choose2ResizeActivity.this, texto2Toast, Toast.LENGTH_SHORT);
        //st.setBackgroundColor(Color.parseColor("#ff5a5f"));
        st.setBackgroundColor(Color.LTGRAY);
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




        // Tell the media scanner about the new file so that it is
        // immediately available to the user.
        MediaScannerConnection.scanFile(this, new String[]{compressedImage.toString()}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                       // Log.i("ExternalStorage", "Scanned " + path + ":");
                       // Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                });



        //TODO animacion o algo



        //Toast.makeText(this, "Compressed image save in " + compressedImage.getPath(), Toast.LENGTH_LONG).show();
       // Log.d("Compressor", "Compressed image save in " + compressedImage.getPath());
    }




////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////CCICLPOS VIDA ADS//////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////



    @Override
    public void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    @Override
    public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }

        recentImages.cleanupCache();
        super.onDestroy();
    }

    public void HelpPulsado(View view) {

        //aqui avamos a poner o quitar la ayuda

        //TODO animar


        final Animation myAnim = AnimationUtils.loadAnimation(this, R.anim.bounce);

        // Use bounce interpolator with amplitude 0.2 and frequency 20
        MyBounceInterpolator interpolator = new MyBounceInterpolator(0.2, 20);
        myAnim.setInterpolator(interpolator);




        TEXTinfo1.setVisibility(View.VISIBLE);
        TEXTinfo2.setVisibility(View.VISIBLE);
        TEXTinfo3.setVisibility(View.VISIBLE);
        FlechaIamgeview.setVisibility(View.VISIBLE);

        TEXTinfo1.startAnimation(myAnim);
        TEXTinfo2.startAnimation(myAnim);
        TEXTinfo3.startAnimation(myAnim);
        FlechaIamgeview.startAnimation(myAnim);


    }
}
