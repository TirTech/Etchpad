package ca.tirtech.etchpad.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import ca.tirtech.etchpad.R;
import ca.tirtech.etchpad.colors.ColorPalette;
import ca.tirtech.etchpad.colors.ColorPaletteWidget;
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
        setViewColor();
        initButtons();
        return root;
    }

    private void initColorPalette() {
        model = new ViewModelProvider(getActivity(), ViewModelProvider.AndroidViewModelFactory.getInstance(getActivity().getApplication())).get(DrawingModel.class);
        newPalette = model.getColorPalette().getValue().clone();
        newPalette.addCallback((obs, cInd)->{
            //Todo biconsumer for color palette object
        });
        paletteWidget = new ColorPaletteEditor(getContext());
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
                currentColor = Color.rgb(r,g,b);
                setViewColor();
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
                currentColor = Color.rgb(r,g,b);
                setViewColor();
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
                currentColor = Color.rgb(r,g,b);
                setViewColor();
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
        currentColor = Color.rgb(r,g,b);
        layout.setBackgroundColor(currentColor);
    }

    public void setCurrentColor(int color){
        //TODO : set the seekbars to the rgb values of the new selected color
        currentColor = color;
        layout.setBackgroundColor(currentColor);
    }

    private void returnToDrawingView(){
        Navigation.findNavController(getActivity(), R.id.fragment).navigate(R.id.action_colorEditorActivity_to_drawingViewFragment);
    }

}
