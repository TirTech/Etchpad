package ca.tirtech.etchpad.colors;

import android.graphics.Color;
import androidx.databinding.Bindable;
import androidx.databinding.library.baseAdapters.BR;
import ca.tirtech.etchpad.mvvm.LiveDataObservable;

import java.util.ArrayList;

public class ColorPalette extends LiveDataObservable {
	
	private ArrayList<Integer> colors = new ArrayList<>();
	private int selectedColor = 0;
	
	public ColorPalette() {
		this.colors.add(Color.rgb(231, 76, 60));
		this.colors.add(Color.rgb(230, 126, 34));
		this.colors.add(Color.rgb(241, 196, 15));
		this.colors.add(Color.rgb(46, 204, 113));
		this.colors.add(Color.rgb(52, 152, 219));
		this.colors.add(Color.rgb(155, 89, 182));
		this.colors.add(Color.rgb(52, 73, 94));
	}
	
	@Bindable
	public ArrayList<Integer> getColors() {
		return colors;
	}
	
	@Bindable
	public Integer getSelectedColor() {
		return colors.get(selectedColor);
	}
	
	public void setSelectedColor(int selectedColor) {
		this.selectedColor = selectedColor;
	}
	
	public void addColor(int color) {
		colors.add(color);
		notifyPropertyChanged(BR.colors);
	}
	
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
	 * Advance to the next color in the palette
	 */
	public void nextColor() {
		selectedColor = (selectedColor + 1) % colors.size();
		notifyPropertyChanged(BR.selectedColor);
	}
	
	public int getSelectedColorIndex() {
		return selectedColor;
	}
}
