package ca.tirtech.etchpad.mvvm;

public class Event<T> {
	private boolean handled;
	private T value;
	
	public Event(T value) {
		this.value = value;
		handled = false;
	}
	
	public boolean isHandled() {
		return handled;
	}
	
	public T peek() {
		return value;
	}
	
	public T consume() {
		handled = true;
		return value;
	}
}
