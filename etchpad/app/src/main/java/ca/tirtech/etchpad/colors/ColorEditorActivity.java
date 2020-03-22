package ca.tirtech.etchpad.colors;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import ca.tirtech.etchpad.R;

public class ColorEditorActivity extends Fragment {
    int currentColor = Color.rgb(128,64,64);
    int r = 128, g = 64, b = 64;
    SeekBar redSeek;
    SeekBar greenSeek;
    SeekBar blueSeek;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initSeekBars();
        setViewColor();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_color_editor, container, false);
    }

    private void initSeekBars() {
        redSeek = getView().findViewById(R.id.seekBar);
        greenSeek = getView().findViewById(R.id.seekBar2);
        blueSeek = getView().findViewById(R.id.seekBar3);

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

    private void setViewColor() {
        currentColor = Color.rgb(r,g,b);
        ConstraintLayout layout = getView().findViewById(R.id.layoutCurrentColor);
        layout.setBackgroundColor(currentColor);
    }

}
