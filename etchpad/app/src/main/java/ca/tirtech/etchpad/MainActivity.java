package ca.tirtech.etchpad;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Bundle;
import android.renderscript.Matrix2f;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

public class MainActivity extends AppCompatActivity {

    private static String TAG = "Main";
    RotationManager rotManager;
    DrawingView drawView;
    Pen pen;

    GestureDetector gd = new GestureDetector(getBaseContext(),new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            pen.setPaintColor(pen.getPaintColor() == Color.RED ? Color.BLUE : Color.RED);
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            Matrix m = new Matrix();
            m.setScale(0.5f,0.5f);
            drawView.path.transform(m);
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            Log.i(TAG,"Saving image...");
            drawView.save();
            return true;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TextView[] views = new TextView[] {
                findViewById(R.id.txtRotX),
                findViewById(R.id.txtRotY),
                findViewById(R.id.txtRotZ)
        };
        drawView = new DrawingView(this,null);
        pen = new Pen(this, drawView);
        rotManager = new RotationManager(this, views, pen::onRotation);
        ((ConstraintLayout)findViewById(R.id.layoutCanvases)).addView(drawView);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gd.onTouchEvent(event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent settings = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(settings);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void onResume() {
        super.onResume();
        rotManager.start();
        pen.start();
    }

    protected void onPause() {
        super.onPause();
        rotManager.stop();
        pen.end();
    }

}
