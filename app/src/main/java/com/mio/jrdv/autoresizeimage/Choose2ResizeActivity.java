package com.mio.jrdv.autoresizeimage;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.amirarcane.recentimages.RecentImages;
import com.amirarcane.recentimages.thumbnailOptions.ImageAdapter;
import com.jess.ui.TwoWayAdapterView;
import com.jess.ui.TwoWayGridView;

import java.io.File;
import java.io.IOException;

public class Choose2ResizeActivity extends AppCompatActivity {





    private Uri imageUri;
    private ImageView imageView;
    private ContentResolver contentResolver;
    private File photoFile = null;
    private RecentImages recentImages;


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

        recentImages.setSize(1);


        final ImageAdapter adapter = recentImages.getAdapter(Choose2ResizeActivity.this);


        twoWayGridView.setAdapter(adapter);
        twoWayGridView.setOnItemClickListener(new TwoWayAdapterView.OnItemClickListener() {
            public void onItemClick(TwoWayAdapterView parent, View v, int position, long id) {
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
                imageView.setImageDrawable(d);
                //mBottomSheetDialog.dismiss();
            }
        });

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
}
