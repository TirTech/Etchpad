package ca.tirtech.etchpad;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

public class MainActivity extends AppCompatActivity {

    RotationManager rotManager;
    DrawingView drawView;
    Pen pen;

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

        if(id == R.id.action_end_path) {
            if (drawView != null) drawView.endPath();
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
