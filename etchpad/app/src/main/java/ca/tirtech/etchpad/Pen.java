package ca.tirtech.etchpad;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.preference.PreferenceManager;

public class Pen {

    private static String TAG = "Pen";

    private DrawingView view;
    private float x;
    private float y;
    private float sensitivity;
    private SharedPreferences sharedPreferences;

    public Pen(Activity owner, DrawingView drawView) {
        this.view = drawView;
        this.x = 1000;//(view.getX() + view.getWidth()/2);
        this.y = 600;//(view.getY() + view.getHeight()/2);
        view.traversePath(x,y);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(owner);
    }

    public void start() {
        this.sensitivity = sharedPreferences.getInt("pen_sensitivity", 10);
        Log.i(TAG,"Sensitivity is " + sensitivity);
    }

    public void end() {

    }

    public void onRotation(float[] vals) {
        x = vals[2] * sensitivity + x;
        y = -1 * vals[1] * sensitivity + y;
        view.traversePath(x,y);
        view.invalidate();
    }
}
