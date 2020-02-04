package ca.tirtech.etchpad;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import androidx.preference.PreferenceManager;

public class Pen {

    private static String TAG = "Pen";

    private DrawingView view;
    private float x;
    private float y;
    private float sensitivity;
    private SharedPreferences sharedPreferences;
    private Paint paint;

    public Pen(Activity owner, DrawingView drawView) {
        this.view = drawView;
        this.x = 1000;//(view.getX() + view.getWidth()/2);
        this.y = 600;//(view.getY() + view.getHeight()/2);
        initPaint(Color.RED);
        view.drawOn(x,y, paint);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(owner);
    }

    public void start() {
        this.sensitivity = sharedPreferences.getInt("pen_sensitivity", 10);
        Log.i(TAG,"Sensitivity is " + sensitivity);
    }

    public void end() {
        //Called when the activity is paused
    }

    public void onRotation(float[] vals) {
        x = vals[2] * sensitivity + x;
        y = -1 * vals[1] * sensitivity + y;
        view.drawOn(x, y, paint);
        view.invalidate();
    }

    private void initPaint(int color) {
        paint = new Paint();
        paint.setAntiAlias(false);
        paint.setColor(color);
        paint.setStrokeJoin(Paint.Join.BEVEL);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5f);
    }

    public void setPaintColor(int color) {
        initPaint(color);
    }

    public int getPaintColor() {
        return paint.getColor();
    }

}
