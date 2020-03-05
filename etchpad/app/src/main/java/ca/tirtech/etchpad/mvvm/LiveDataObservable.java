package ca.tirtech.etchpad.mvvm;

import androidx.databinding.BaseObservable;
import androidx.databinding.Observable;

import java.util.function.BiConsumer;

/**
 * LiveDataObservable is a helper class assisting in providing object property observation in LiveData objects.
 * This class contains a single callback assigned by LiveData and stored in this object. This class must contain the assigned
 * observable as it must be removed when the LiveData value is set to another object. The callback is reduced to a {@link BiConsumer}
 * for lambda compatibility and simplicity. Objects wishing to be deeply observed should extend this class instead of {@link BaseObservable}.
 * This class must be used in conjunction with {@link DeepLiveData} to provide callback assignment. While it may be possible to use this class
 * for other purposes, this class is designed for DeepLiveData.
 */
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
	
	/**
	 * Default blank constructor.
	 */
	public LiveDataObservable() {
	}
	
	/**
	 * Set the callback for this observable's property changes.
	 *
	 * @param callbackFunc the function to call on property changes
	 */
	public void addCallback(BiConsumer<Observable, Integer> callbackFunc) {
		this.addOnPropertyChangedCallback(callback);
		this.callbackFunc = callbackFunc;
	}
	
	/**
	 * Remove the callback for this observable's property changes.
	 */
	public void removeCallback() {
		this.removeOnPropertyChangedCallback(callback);
	}
}
