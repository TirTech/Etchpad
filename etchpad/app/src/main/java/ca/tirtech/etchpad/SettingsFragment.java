package ca.tirtech.etchpad;

import android.os.Bundle;
import androidx.preference.PreferenceFragmentCompat;

/**
 * Fragment for the Settings Activity.
 */
public class SettingsFragment extends PreferenceFragmentCompat {
	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		setPreferencesFromResource(R.xml.preferences, rootKey);
	}
}
