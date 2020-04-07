package ca.tirtech.etchpad.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.SeekBar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import ca.tirtech.etchpad.R;
import ca.tirtech.etchpad.drawingView.DrawingModel;
import ca.tirtech.etchpad.drawingView.DrawingView;
import ca.tirtech.etchpad.drawingView.network.DrawingProtocol;
import ca.tirtech.etchpad.hardware.InteractionService;
import com.google.android.material.snackbar.Snackbar;

/**
 * Fragment for drawing.
 */
public class DrawingViewFragment extends Fragment {
	
	private static final String TAG = "Drawing Fragment";
	private DrawingView drawView;
	private DrawingProtocol drawingProtocol;
	private DrawingModel model;
	private boolean connected = false;
	
	private final SeekBar.OnSeekBarChangeListener penSizeChangeListener = new SeekBar.OnSeekBarChangeListener() {
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			model.setPaintSize(progress);
			drawView.invalidate();
		}
		
		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
		}
		
		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
		}
	};
	private SeekBar penSize;
	
	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		ConstraintLayout root = (ConstraintLayout) inflater.inflate(R.layout.fragment_drawing_view, container, false);
		setHasOptionsMenu(true);
		Toolbar appBarLayout = getActivity().findViewById(R.id.app_toolbar);
		if (appBarLayout != null) {
			appBarLayout.setTitle(R.string.app_name);
		}
		drawView = root.findViewById(R.id.drawingView);
		penSize = root.findViewById(R.id.sliderPenSize);
		root.setOnTouchListener((v, e) -> InteractionService.onTouchEvent(e));
		penSize.setOnSeekBarChangeListener(penSizeChangeListener);
		initModel();
		return root;
	}
	
	private void initModel() {
		model = new ViewModelProvider(getActivity(), ViewModelProvider.AndroidViewModelFactory.getInstance(getActivity().getApplication())).get(DrawingModel.class);
		model.getLayer().observe(getViewLifecycleOwner(), (layer) -> {
			if (layer.getPaintSize() != penSize.getProgress()) {
				penSize.setProgress((int) layer.getPaintSize());
			}
		});
	}
	
	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		int id = item.getItemId();
		
		switch (id) {
			case R.id.action_export:
				Log.i(TAG, "Exporting as JPG...");
				model.export(drawView.getContext(), drawView.getWidth(), drawView.getHeight());
				return true;
			case R.id.action_save:
				Log.i(TAG, "Saving as JSON ...");
				model.save(drawView.getContext());
				return true;
			case R.id.action_load:
				Log.i(TAG, "Loading JSON ...");
				model.load(drawView.getContext());
				return true;
			case R.id.action_clear:
				Log.i(TAG, "Cleared Screen");
				model.getLayer().getValue().clear();
				model.getLayer().getValue().centerOnCursor();
				return true;
			case R.id.action_host:
				if (drawingProtocol != null) {
					Snackbar.make(getView(), R.string.snack_already_connected, Snackbar.LENGTH_LONG).show();
					return true;
				}
				Log.i(TAG, "Hosting...");
				drawingProtocol = new DrawingProtocol(getActivity(), model);
				drawingProtocol.setOnDisconnectCallback(this::onDrawingProtocolDisconnect);
				drawingProtocol.host();
				this.connected = true;
				getActivity().invalidateOptionsMenu();
				return true;
			case R.id.action_join:
				if (drawingProtocol != null) {
					Snackbar.make(getView(), R.string.snack_already_connected, Snackbar.LENGTH_LONG).show();
					return true;
				}
				Log.i(TAG, "Joining...");
				drawingProtocol = new DrawingProtocol(getActivity(), model);
				drawingProtocol.setOnDisconnectCallback(this::onDrawingProtocolDisconnect);
				drawingProtocol.join();
				this.connected = true;
				getActivity().invalidateOptionsMenu();
				return true;
			case R.id.action_disconnect:
				if (drawingProtocol != null) {
					drawingProtocol.disconnect();
				} else {
					Snackbar.make(getView(), R.string.snack_not_connected, Snackbar.LENGTH_LONG).show();
				}
				return true;
			case R.id.action_help:
				Navigation.findNavController(getActivity(), R.id.fragment).navigate(R.id.action_drawing_view_to_help);
				return true;
			case R.id.action_center:
				model.getLayer().getValue().centerOnCursor();
				return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		Log.i(TAG, "Resume");
		InteractionService.enable();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		Log.i(TAG, "Pause");
		InteractionService.disable();
	}
	
	@Override
	public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		menu.findItem(R.id.action_disconnect).setEnabled(this.connected);
		menu.findItem(R.id.action_host).setEnabled(!this.connected);
		menu.findItem(R.id.action_join).setEnabled(!this.connected);
	}
	
	private void onDrawingProtocolDisconnect() {
		Log.i("TEST", "On Disconnect was called");
		this.connected = false;
		this.drawingProtocol = null;
		getActivity().invalidateOptionsMenu();
	}
}
