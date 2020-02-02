package ca.tirtech.etchpad;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

public class MainActivity extends AppCompatActivity {

    RotationManager rotManager;
    DrawingView drawView;

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
        Pen pen = new Pen(drawView);
        rotManager = new RotationManager(this, views, 0.1f,pen::onRotation);
        ((ConstraintLayout)findViewById(R.id.layoutCanvases)).addView(drawView);
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
            return true;
        }

        if(id == R.id.action_end_path) {
            if (drawView != null) drawView.endPath();
        }

        return super.onOptionsItemSelected(item);
    }

    protected void onResume() {
        super.onResume();
        rotManager.start();
    }

    protected void onPause() {
        super.onPause();
        rotManager.stop();
    }

}
