package ca.tirtech.etchpad.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.library.baseAdapters.BR;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import ca.tirtech.etchpad.R;
import ca.tirtech.etchpad.colors.ColorPalette;
import ca.tirtech.etchpad.colors.ColorPaletteEditor;
import ca.tirtech.etchpad.drawingView.DrawingModel;

public class ColorEditorFragment extends Fragment {
    int currentColor = Color.rgb(128,64,64);
    int r = 128, g = 64, b = 64;
    ConstraintLayout layout, colorPaletteContainer;
    View root;
    SeekBar redSeek, greenSeek, blueSeek;
    Button applyBtn, cancelBtn, addBtn, removeBtn;
    ColorPalette newPalette;
    ColorPaletteEditor paletteWidget;
    DrawingModel model;

    //to set or get use .set() or .get() for live data color palette objects


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	    root = inflater.inflate(R.layout.activity_color_editor, container, false);
	    layout = root.findViewById(R.id.layoutCurrentColor);
	    colorPaletteContainer = root.findViewById(R.id.layoutColors);
	
	    initColorPalette();
	    initSeekBars();
	    setCurrentColor(newPalette.getSelectedColor());
	    initButtons();
	    return root;
    }
	
	/**
	 * Set up the color Palette from the model and initialize widgets.
	 */
	private void initColorPalette() {
		model = new ViewModelProvider(getActivity(), ViewModelProvider.AndroidViewModelFactory.getInstance(getActivity().getApplication())).get(DrawingModel.class);
		newPalette = model.getColorPalette().getValue().clone();
		newPalette.addCallback((obs, propId) -> {
			if (propId == BR.selectedColor) {
				setCurrentColor(newPalette.getSelectedColor());
			}
		});
		paletteWidget = new ColorPaletteEditor(getContext(), newPalette);
		colorPaletteContainer.addView(paletteWidget);
	}

    private void initSeekBars() {
        redSeek = root.findViewById(R.id.seekBar);
        greenSeek = root.findViewById(R.id.seekBar2);
        blueSeek = root.findViewById(R.id.seekBar3);

        redSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
	            r = progress;
	            if (fromUser) setViewColor();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        greenSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
	            g = progress;
	            if (fromUser) setViewColor();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        blueSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                b = progress;
	            if (fromUser) setViewColor();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void initButtons(){
        applyBtn = root.findViewById(R.id.button);
        cancelBtn = root.findViewById(R.id.button2);
        addBtn = root.findViewById(R.id.button3);
        removeBtn = root.findViewById(R.id.button4);

        //set button actions
        applyBtn.setOnClickListener(v -> {
            model.getColorPalette().setValue(newPalette);
            returnToDrawingView();
        });

        cancelBtn.setOnClickListener(v -> returnToDrawingView());

        addBtn.setOnClickListener(v -> {

        });

        removeBtn.setOnClickListener(v -> {
	
        });
    }
	
	private void setViewColor() {
		currentColor = Color.rgb(r, g, b);
		layout.setBackgroundColor(currentColor);
		newPalette.setColor(newPalette.getSelectedColorIndex(), currentColor);
		paletteWidget.computePaints();
	}
	
	/**
	 * Sets the shown color of the editor and sets bars appropriately.
	 *
	 * @param color the color to set
	 */
	public void setCurrentColor(int color) {
		currentColor = color;
		redSeek.setProgress(Color.red(currentColor));
		greenSeek.setProgress(Color.green(currentColor));
		blueSeek.setProgress(Color.blue(currentColor));
		layout.setBackgroundColor(currentColor);
	}
	
	private void returnToDrawingView() {
		Navigation.findNavController(getActivity(), R.id.fragment).navigate(R.id.action_colorEditorActivity_to_drawingViewFragment);
    }

}
