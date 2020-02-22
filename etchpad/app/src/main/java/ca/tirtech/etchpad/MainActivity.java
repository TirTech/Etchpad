package ca.tirtech.etchpad;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import ca.tirtech.etchpad.drawingView.DrawingView;
import ca.tirtech.etchpad.drawingView.network.DrawingProtocol;
import ca.tirtech.etchpad.hardware.InteractionService;

/**
 * Main app activity. Contains the drawing view and menus.
 */
public class MainActivity extends AppCompatActivity {
	
	private static final String TAG = "Main";
	private DrawingView drawView;
	private DrawingProtocol drawingProtocol;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		InteractionService.init(this);
		setContentView(R.layout.activity_main);
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		drawView = findViewById(R.id.drawingView);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return InteractionService.onTouchEvent(event);
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
				drawView.getModel().export(drawView.getContext(), drawView.getWidth(), drawView.getHeight());
				return true;
			case R.id.action_save:
				Log.i(TAG, "Saving as JSON ...");
				drawView.getModel().save(drawView.getContext());
				return true;
			case R.id.action_load:
				Log.i(TAG, "Loading JSON ...");
				drawView.getModel().load(drawView.getContext());
				return true;
			case R.id.action_clear:
				Log.i(TAG, "Cleared Screen");
				drawView.getModel().getLayer().getValue().clear();
				return true;
			case R.id.action_host:
				Log.i(TAG, "Hosting...");
				drawingProtocol = new DrawingProtocol(this, drawView.getModel());
				drawingProtocol.host();
				return true;
			case R.id.action_join:
				Log.i(TAG, "Joining...");
				drawingProtocol = new DrawingProtocol(this, drawView.getModel());
				drawingProtocol.join();
				return true;
			case R.id.action_disconnect:
				if (drawingProtocol != null) {
					drawingProtocol.disconnect();
				}
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	protected void onResume() {
		super.onResume();
		InteractionService.enable();
	}
	
	protected void onPause() {
		super.onPause();
		InteractionService.disable();
	}
	
}
