package ca.tirtech.etchpad;

import android.util.Log;

public class Pen {

    private static final float SCALE = 50;
    private static String TAG = "Pen";

    private DrawingView view;
    private float x;
    private float y;

    public Pen(DrawingView drawView) {
        this.view = drawView;
        this.x = 1000;//(view.getX() + view.getWidth()/2);
        this.y = 600;//(view.getY() + view.getHeight()/2);
        view.traversePath(x,y);
    }

    public void onRotation(float[] vals) {
        x = vals[2] * SCALE + x;
        y = -1 * vals[1] * SCALE + y;
        Log.i(TAG, "X=" + x + ", Y=" + y);
        view.traversePath(x,y);
        view.invalidate();
    }
}
