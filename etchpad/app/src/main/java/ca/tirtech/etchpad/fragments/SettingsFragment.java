package ca.tirtech.etchpad.fragments;

import android.os.Bundle;
import androidx.preference.PreferenceFragmentCompat;
import ca.tirtech.etchpad.R;

/**
 * Fragment for the Settings Activity.
 */
public class SettingsFragment extends PreferenceFragmentCompat {
	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		setPreferencesFromResource(R.xml.preferences, rootKey);
	}
}
