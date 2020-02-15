package ca.tirtech.etchpad.mvvm;

import androidx.databinding.BaseObservable;
import androidx.databinding.Observable;

import java.util.function.BiConsumer;

public class LiveDataObservable extends BaseObservable {
	
	private BiConsumer<Observable, Integer> callbackFunc = null;
	private final OnPropertyChangedCallback callback = new OnPropertyChangedCallback() {
		@Override
		public void onPropertyChanged(Observable sender, int propertyId) {
			if (callbackFunc != null) {
				callbackFunc.accept(sender, propertyId);
			}
		}
	};
	
	public LiveDataObservable() {
	}
	
	public void addCallback(BiConsumer<Observable, Integer> callbackFunc) {
		this.addOnPropertyChangedCallback(callback);
		this.callbackFunc = callbackFunc;
	}
	
	public void removeCallback() {
		this.removeOnPropertyChangedCallback(callback);
	}
}
