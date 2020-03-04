package ca.tirtech.etchpad.hardware;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.util.Consumer;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class PermissionManager {
	
	private ArrayList<String> perms = new ArrayList<>();
	private Consumer<Boolean> callback;
	
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
	
	private boolean check(String permission, Activity activity) {
		return (ActivityCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED);
	}
	
	public void checkAllPermissions(Activity activity) {
		if (perms.size() > 0) {
			perms.stream().filter(v -> check(v, activity)).collect(Collectors.toCollection(ArrayList::new)).forEach(v -> perms.remove(v));
			String[] p = new String[perms.size()];
			if (perms.size() > 0) ActivityCompat.requestPermissions(activity, perms.toArray(p), 1);
			else callback.accept(true);
		} else callback.accept(true);
	}
	
	public void onPermissionCallback(int requestCode, String[] permissions, int[] grantResults) {
		for (int i = 0; i < grantResults.length; i++) {
			if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
				perms.remove(permissions[i]);
			}
		}
		if (perms.size() == 0) callback.accept(true);
	}
}
