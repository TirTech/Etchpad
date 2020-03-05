package ca.tirtech.etchpad;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RawRes;
import androidx.annotation.StringRes;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

/**
 * Main help menu providing options for help and information.
 */
public class HelpMenuFragment extends Fragment {
	
	@SuppressLint ("ClickableViewAccessibility")
	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		ConstraintLayout root = (ConstraintLayout) inflater.inflate(R.layout.fragment_help_list, container, false);
		root.findViewById(R.id.help_item_about).setOnTouchListener((view, event) -> {
			if (event.getAction() == MotionEvent.ACTION_DOWN) navigateToHelp(R.string.help_card_about, R.raw.help_about);
			return true;
		});
		return root;
	}
	
	/**
	 * Navigate to the help detail fragment.
	 *
	 * @param title the id of the title text
	 * @param text  the id of the body text
	 */
	private void navigateToHelp(@StringRes int title, @RawRes int text) {
		Bundle arguments = new Bundle();
		arguments.putInt(HelpDetailFragment.ARG_TITLE_ID, title);
		arguments.putInt(HelpDetailFragment.ARG_ITEM_ID, text);
		HelpDetailFragment fragment = new HelpDetailFragment();
		fragment.setArguments(arguments);
		if (getActivity() != null) ((HelpActivity) getActivity()).changeFragment(fragment, true);
	}
	
}
