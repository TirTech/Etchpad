package ca.tirtech.etchpad;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.util.Pair;

import java.util.Stack;

public class DrawingView extends View {

    private static String TAG = "Drawing View";
    Paint paint;
    Path path;
    Stack<Pair<Path,Paint>> paths = new Stack<>();

    public DrawingView(Context context) {
        super(context);
        initPaint();
    }

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaint();
    }

    private void initPaint() {
        paint = new Paint();
        paint.setAntiAlias(false);
        paint.setColor(Color.RED);
        paint.setStrokeJoin(Paint.Join.BEVEL);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5f);
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

    public void traversePath(float x, float y) {
        if (paint == null) {
            initPaint();
        }
        if (path == null) {
            path = new Path();
            path.moveTo(x,y);
            return;
        }
        path.lineTo(x,y);
    }

    public void endPath() {
        paths.push(new Pair<>(path, paint));
        path = null;
        paint = null;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            paint.setColor(paint.getColor() == Color.RED ? Color.BLUE : Color.RED);
        }
        return true;
    }
}
