package ca.tirtech.etchpad.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import ca.tirtech.etchpad.R;
import ca.tirtech.etchpad.drawingView.DrawingModel;
import ca.tirtech.etchpad.drawingView.DrawingView;
import ca.tirtech.etchpad.drawingView.network.DrawingProtocol;
import ca.tirtech.etchpad.hardware.InteractionService;

/**
 * Fragment for drawing.
 */
public class DrawingViewFragment extends Fragment {
	
	private static final String TAG = "Drawing Fragment";
	private DrawingView drawView;
	private DrawingProtocol drawingProtocol;
	private DrawingModel model;
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
				drawView.getModel().export(drawView.getContext(), drawView.getWidth(), drawView.getHeight());
				return true;
			case R.id.action_save:
				Log.i(TAG, "Saving as JSON ...");
				drawView.getModel().save(drawView.getContext());
				return true;
			case R.id.action_load:
				Log.i(TAG, "Loading JSON ...");
				drawView.getModel().load(drawView.getContext());
				return true;
			case R.id.action_clear:
				Log.i(TAG, "Cleared Screen");
				drawView.getModel().getLayer().getValue().clear();
				return true;
			case R.id.action_host:
				Log.i(TAG, "Hosting...");
				drawingProtocol = new DrawingProtocol(getActivity(), drawView.getModel());
				drawingProtocol.host();
				return true;
			case R.id.action_join:
				Log.i(TAG, "Joining...");
				drawingProtocol = new DrawingProtocol(getActivity(), drawView.getModel());
				drawingProtocol.join();
				return true;
			case R.id.action_disconnect:
				if (drawingProtocol != null) {
					drawingProtocol.disconnect();
				}
				return true;
			case R.id.action_help:
				Navigation.findNavController(getActivity(), R.id.fragment).navigate(R.id.action_drawing_view_to_help);
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
}
