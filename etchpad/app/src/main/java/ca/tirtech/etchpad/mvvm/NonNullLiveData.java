package ca.tirtech.etchpad.mvvm;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import java.util.Objects;

/**
 * Extension of MutableLiveData disallowing null values.
 *
 * @param <T>
 */
public class NonNullLiveData<T> extends MutableLiveData<T> {
	
	/**
	 * Create a new LiveData with an initial value.
	 *
	 * @param initalValue the value to set
	 */
	public NonNullLiveData(@NonNull T initalValue) {
		super(initalValue);
	}
	
	@NonNull
	@Override
	public T getValue() {
		return Objects.requireNonNull(super.getValue());
	}
	
	@Override
	public void setValue(@NonNull T value) {
		super.setValue(value);
	}
}
