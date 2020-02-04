package ca.tirtech.etchpad;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;

import androidx.core.util.Pair;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Stack;

public class DrawingView extends View {

    private static String TAG = "Drawing View";

    Path path;
    Paint paint;
    Stack<Pair<Path,Paint>> paths = new Stack<>();
    float x;
    float y;

    public DrawingView(Context context) {
        super(context);
    }

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(path != null) {
            canvas.drawPath(path,paint);
        }
        for (Pair<Path,Paint> p : paths) {
            canvas.drawPath(p.first,p.second);
        }
    }

    public void drawOn(float x, float y, Paint paint) {

        if (this.paint == null) {
            this.paint = paint;
        } else if (this.paint != paint) {
            if (path != null){
                paths.push(new Pair<>(path, new Paint(this.paint)));
            }
            path = null;
            this.paint = paint;
        }

        if (path == null) {
            path = new Path();
            path.moveTo(this.x,this.y);
        }

        path.lineTo(x,y);
        this.x = x;
        this.y = y;
    }

    public void save() {
        try {
            // Create an image file name
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            final File image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );

            setDrawingCacheEnabled(true);
            Bitmap b = getDrawingCache();
            new Thread(() -> {
                try {
                    FileOutputStream fos = new FileOutputStream(image);
                    b.compress(Bitmap.CompressFormat.JPEG, 95, fos);
                    fos.flush();
                    fos.close();

                    MediaStore.Images.Media.insertImage(getContext().getContentResolver(),image.getAbsolutePath(),image.getName(),image.getName());
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                }
            }).start();
        } catch (IOException ex) {
            Log.e(TAG,ex.getMessage());
        }
    }
}
