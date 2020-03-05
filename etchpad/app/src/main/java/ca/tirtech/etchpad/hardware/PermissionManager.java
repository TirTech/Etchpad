package ca.tirtech.etchpad.hardware;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.util.Consumer;

import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Helper class for permissions.
 */
public class PermissionManager {
	
	private final ArrayList<String> perms = new ArrayList<>();
	private final Consumer<Boolean> callback;
	
	/**
	 * Set up the manager with all permissions for the app.
	 *
	 * @param callback the callback to invoke when permissions have all been handled
	 */
	public PermissionManager(Consumer<Boolean> callback) {
		this.callback = callback;
		perms.add(Manifest.permission.READ_EXTERNAL_STORAGE);
		perms.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
		perms.add(Manifest.permission.BLUETOOTH);
		perms.add(Manifest.permission.BLUETOOTH_ADMIN);
		perms.add(Manifest.permission.ACCESS_WIFI_STATE);
		perms.add(Manifest.permission.CHANGE_WIFI_STATE);
		perms.add(Manifest.permission.ACCESS_COARSE_LOCATION);
		perms.add(Manifest.permission.ACCESS_FINE_LOCATION);
	}
	
	/**
	 * Check if the permission is already granted.
	 *
	 * @param permission the permission to check
	 * @param activity   the activity the permission would be granted to
	 * @return whether the permission is granted
	 */
	private boolean check(String permission, Activity activity) {
		return (ActivityCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED);
	}
	
	/**
	 * Check all permissions and prompt for missing ones.
	 *
	 * @param activity the activity to check for
	 */
	public void checkAllPermissions(Activity activity) {
		if (perms.size() > 0) {
			perms.stream().filter(v -> check(v, activity)).collect(Collectors.toCollection(ArrayList::new)).forEach(perms::remove);
			String[] p = new String[perms.size()];
			if (perms.size() > 0) ActivityCompat.requestPermissions(activity, perms.toArray(p), 1);
			else callback.accept(true);
		} else callback.accept(true);
	}
	
	/**
	 * Callback for when permission dialog results have been returned. Should be invoked from the activity.
	 *
	 * @param requestCode  the permission request's code
	 * @param permissions  the permissions requested
	 * @param grantResults the permission request statuses (pairs with permissions)
	 */
	public void onPermissionCallback(@SuppressWarnings ("unused") int requestCode, String[] permissions, int[] grantResults) {
		for (int i = 0; i < grantResults.length; i++) {
			if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
				perms.remove(permissions[i]);
			}
		}
		if (perms.size() == 0) callback.accept(true);
	}
}
