package ca.tirtech.etchpad.mvvm;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

/**
 * Extension of {@link MutableLiveData} allowing for the value to be observed for changes.
 * Changes to the value of properties exposed by the current value of this object will cause
 * observers of DeepLiveData to be notified as if the entire object was replaced.
 * <p/>
 * The value of DeepLiveData must extend {@link LiveDataObservable} in order to ensure removal
 * of callbacks when the value is changed to a different object.
 *
 * @param <T>
 */
public class DeepLiveData<T extends LiveDataObservable>
		extends NonNullLiveData<T> {
	
	/**
	 * Create a new LiveData instance.
	 *
	 * @param initialValue the initial value to set
	 */
	public DeepLiveData(@NonNull T initialValue) {
		super(initialValue);
		setValue(getValue());
	}
	
	/**
	 * Sets the value. If there are active observers, the value will be dispatched to them.
	 * <p>
	 * Values assigned using this function will be bound to. When changed, all active observers will be notified.
	 * <p>
	 * Old values will be unbound only when replaced with another {@link LiveDataObservable} or {@code null}.
	 *
	 * @param value The new value
	 */
	@Override
	public void setValue(@NonNull T value) {
		getValue().removeCallback();
		super.setValue(value);
		getValue().addCallback((s, p) -> setValue(getValue()));
	}
}
