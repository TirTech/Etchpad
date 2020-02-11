package ca.tirtech.etchpad;

import androidx.lifecycle.MutableLiveData;

public class DeepLiveData<T extends LiveDataObservable>
		extends MutableLiveData<T> {
	
	@Override
	public void setValue(T value) {
		if (getValue() != null) {
			getValue().removeCallback();
		}
		super.setValue(value);
		getValue().addCallback((s, p) -> setValue(getValue()));
	}
}
