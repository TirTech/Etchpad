package ca.tirtech.etchpad.fragments;

import android.os.Bundle;
import android.view.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceFragmentCompat;
import ca.tirtech.etchpad.R;

/**
 * Fragment for the Settings Activity.
 */
public class SettingsFragment extends PreferenceFragmentCompat {
	
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View v = super.onCreateView(inflater, container, savedInstanceState);
		setHasOptionsMenu(true);
		Toolbar appBarLayout = getActivity().findViewById(R.id.app_toolbar);
		if (appBarLayout != null) {
			appBarLayout.setTitle(R.string.action_settings);
		}
		return v;
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		menu.clear();
	}
	
	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		setPreferencesFromResource(R.xml.preferences, rootKey);
	}
}
