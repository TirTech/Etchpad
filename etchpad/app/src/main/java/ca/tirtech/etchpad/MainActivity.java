package ca.tirtech.etchpad;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.Navigation;
import ca.tirtech.etchpad.hardware.InteractionService;
import ca.tirtech.etchpad.hardware.PermissionManager;
import com.google.android.material.snackbar.Snackbar;

/**
 * Main app activity. Contains the drawing view and menus.
 */
public class MainActivity extends AppCompatActivity {
	
	private PermissionManager permissionManager;
	private static final String TAG = "Main";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		InteractionService.init(this);
		setContentView(R.layout.activity_main);
		Toolbar toolbar = findViewById(R.id.help_toolbar);
		setSupportActionBar(toolbar);
		permissionManager = new PermissionManager(v -> Snackbar.make(findViewById(R.id.activity_main), "Permissions ok", Snackbar.LENGTH_LONG).show());
		permissionManager.checkAllPermissions(this);
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
				Navigation.findNavController(this, R.id.fragment).navigate(R.id.action_drawing_view_to_settings);
				return true;
			case R.id.action_help:
				Navigation.findNavController(this, R.id.fragment).navigate(R.id.action_drawing_view_to_help);
				return true;
		}
		
		return false;
	}
	
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		permissionManager.onPermissionCallback(requestCode, permissions, grantResults);
	}
}
