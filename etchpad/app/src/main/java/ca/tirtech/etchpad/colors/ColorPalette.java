package ca.tirtech.etchpad.colors;

import android.graphics.Color;
import androidx.databinding.Bindable;
import androidx.databinding.library.baseAdapters.BR;
import ca.tirtech.etchpad.mvvm.LiveDataObservable;

import java.util.ArrayList;

/**
 * A container class for lists of colors. One color may be selected and cycled trough using {@link #nextColor()}.
 * Colors may be added and removed from the palette up to an unlimited amount
 */
public class ColorPalette extends LiveDataObservable {
	
	/**
	 * List of all colors contained in the palette.
	 */
	private ArrayList<Integer> colors = new ArrayList<>();
	
	/**
	 * Index of color that is selected in the {@link #colors} list.
	 */
	private int selectedColor = 0;
	
	/**
	 * Construct a ColorPalette containing the default colors.
	 */
	public ColorPalette() {
		this.colors.add(Color.rgb(231, 76, 60));
		this.colors.add(Color.rgb(230, 126, 34));
		this.colors.add(Color.rgb(241, 196, 15));
		this.colors.add(Color.rgb(46, 204, 113));
		this.colors.add(Color.rgb(52, 152, 219));
		this.colors.add(Color.rgb(155, 89, 182));
		this.colors.add(Color.rgb(52, 73, 94));

	}

	/**
	 * Sets the colors for this palette.
	 * @param colors the colors to set
	 */
	public void setColors(ArrayList<Integer> colors) {
		this.colors = colors;
	}

	/**
	 * Returns the colors contained in the palette.
	 *
	 * @return the palettes colors
	 */
	@Bindable
	public ArrayList<Integer> getColors() {
		return colors;
	}
	
	/**
	 * Returns the selected color.
	 *
	 * @return selected color
	 */
	@Bindable
	public Integer getSelectedColor() {
		return colors.get(selectedColor);
	}
	
	/**
	 * Sets the selected color index.
	 *
	 * @param selectedColor the index of the color to select
	 */
	public void setSelectedColor(int selectedColor) {
		this.selectedColor = selectedColor;
	}
	
	/**
	 * Adds a color to the palette. This method allows for duplicate colors.
	 *
	 * @param color the color to add
	 */
	public void addColor(int color) {
		colors.add(color);
		notifyPropertyChanged(BR.colors);
	}
	
	/**
	 * Removes the first instance of the specified color from the palette.
	 *
	 * @param color the color to remove
	 */
	public void removeColor(int color) {
		for (int i = colors.size() - 1; i >= 0; i--) {
			if (colors.get(i) == color) {
				colors.remove(i);
				break;
			}
		}
		notifyPropertyChanged(BR.colors);
	}
	
	/**
	 * Advance to the next color in the palette.
	 */
	public void nextColor() {
		selectedColor = (selectedColor + 1) % colors.size();
		notifyPropertyChanged(BR.selectedColor);
	}
	
	/**
	 * Returns the index of the selected color.
	 *
	 * @return selected color index
	 */
	public int getSelectedColorIndex() {
		return selectedColor;
	}
}
