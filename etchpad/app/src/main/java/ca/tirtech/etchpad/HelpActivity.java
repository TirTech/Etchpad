package ca.tirtech.etchpad;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

/**
 * Activity for showing help information and app details.
 */
public class HelpActivity extends AppCompatActivity {
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help);
		changeFragment(new HelpMenuFragment(), false);
	}
	
	/**
	 * Change the fragment displayed by the activity.
	 *
	 * @param fragment       the new fragment to display.
	 * @param addToBackstack whether to add this to the backstack
	 */
	public void changeFragment(Fragment fragment, boolean addToBackstack) {
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		
		if (addToBackstack) {
			transaction.addToBackStack(null);
			transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		}
		transaction.replace(R.id.help_fragment_container, fragment).commit();
	}
}
