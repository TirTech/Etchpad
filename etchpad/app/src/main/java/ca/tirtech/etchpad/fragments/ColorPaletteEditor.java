package ca.tirtech.etchpad.fragments;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

import androidx.annotation.Nullable;
import androidx.navigation.Navigation;

import java.util.ArrayList;

import ca.tirtech.etchpad.R;
import ca.tirtech.etchpad.colors.ColorPaletteWidget;

public class ColorPaletteEditor extends ColorPaletteWidget {


    private final GestureDetector gd = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            float x = e.getX();

            return true;

            /* what I had in cpwidget

            float x = e.getX();
			int width = ColorPaletteWidget.this.getWidth();
			ArrayList<Integer> palette = model.getColorPalette().getValue().getColors();
			int selectedColor = (int)((x/width) * palette.size());
			Log.i("color: ",selectedColor+"");
			model.getColorPalette().getValue().setSelectedColor(selectedColor);
			model.setSelectedWidgetColor(selectedColor);
			invalidate();

             */
        }


        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }
    });

    public ColorPaletteEditor(Context context) {
        super(context);
    }

    public ColorPaletteEditor(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }
}
