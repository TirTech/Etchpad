package ca.tirtech.etchpad;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity {
	
	private static final String TAG = "Main";
	private RotationManager rotManager;
	private DrawingView drawView;
	
	private final GestureDetector gd = new GestureDetector(getBaseContext(), new GestureDetector.SimpleOnGestureListener() {
		
		@Override
		public boolean onDoubleTap(MotionEvent e) {
			//Double tap
			return true;
		}
		
		@Override
		public void onLongPress(MotionEvent e) {
           /* Matrix m = new Matrix();
            m.setScale(0.5f,0.5f);
            drawView.path.transform(m);*/
		}
		
		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			drawView.setPaintColor(drawView.getPaintColor() == Color.RED ? Color.BLUE : Color.RED);
			Log.i(TAG, "Changed Color");
			return true;
		}
	});
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		drawView = findViewById(R.id.drawingView);
		rotManager = new RotationManager(this);
		rotManager.setRotationListener(drawView::onRotation);
		rotManager.setShakeListener(drawView::onShake);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return gd.onTouchEvent(event);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		
		switch (id) {
			case R.id.action_settings:
				Intent settings = new Intent(MainActivity.this, SettingsActivity.class);
				startActivity(settings);
				return true;
			case R.id.action_export:
				Log.i(TAG, "Exporting as JPG...");
				drawView.export();
				return true;
			case R.id.action_save:
				Log.i(TAG, "Saving as JSON ...");
				drawView.save();
				return true;
			case R.id.action_load:
				Log.i(TAG, "Loading JSON ...");
				drawView.load();
				return true;
			case R.id.action_clear:
				Log.i(TAG, "Cleared Screen");
				drawView.clear();
				return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	protected void onResume() {
		super.onResume();
		rotManager.start();
		drawView.resume();
	}
	
	protected void onPause() {
		super.onPause();
		rotManager.stop();
		drawView.pause();
	}
	
}
