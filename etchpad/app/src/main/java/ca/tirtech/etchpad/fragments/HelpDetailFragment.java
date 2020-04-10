package ca.tirtech.etchpad.fragments;

import android.os.Bundle;
import android.text.Spanned;
import android.util.Log;
import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RawRes;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import ca.tirtech.etchpad.R;
import io.noties.markwon.Markwon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Fragment for displaying help information for a selected item.
 */
public class HelpDetailFragment extends Fragment {
	
	private static final String TAG = "HelpDetailFragment";
	
	/**
	 * The resource id of the item to show.
	 */
	public static final String ARG_ITEM_ID = "Item";
	/**
	 * The resource id of the title to show.
	 */
	public static final String ARG_TITLE_ID = "Title";
	
	@RawRes
	private int resId;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		
		if (getArguments() != null && getArguments().containsKey(ARG_ITEM_ID) && getArguments().containsKey(ARG_TITLE_ID) && getActivity() != null) {
			resId = getArguments().getInt(ARG_ITEM_ID);
			Toolbar appBarLayout = getActivity().findViewById(R.id.app_toolbar);
			if (appBarLayout != null) {
				appBarLayout.setTitle(getArguments().getInt(ARG_TITLE_ID));
			}
		} else {
			resId = -1;
		}
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		menu.clear();
	}
	
	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.fragment_help_detail, container, false);
		if (resId != -1 && getContext() != null) {
			TextView view = root.findViewById(R.id.txt_help_details);
			BufferedReader in = new BufferedReader(new InputStreamReader(getResources().openRawResource(resId)));
			String data = in.lines().reduce("", (acc, v) -> acc + v + "\n");
			Markwon mw = Markwon.create(getContext());
			Spanned text = mw.toMarkdown(data);
			view.setText(text);
			try {
				in.close();
			} catch (IOException e) {
				Log.e(TAG, "An error occurred closing a reader: " + (e.getMessage() != null ? e.getMessage() : "Unknown"));
			}
		}
		return root;
	}
}
