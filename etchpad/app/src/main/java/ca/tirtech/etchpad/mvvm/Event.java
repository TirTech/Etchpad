package ca.tirtech.etchpad.mvvm;

/**
 * Class for providing consumable values. This classes primary purpose is to facilitate events through LiveData by
 * allowing the value to be consumed by the observer.
 *
 * @param <T> the type of the stored value
 */
public class Event<T> {
	private boolean handled;
	private final T value;
	
	/**
	 * Construct a new event. The value set is final.
	 *
	 * @param value the value for this event
	 */
	public Event(T value) {
		this.value = value;
		handled = false;
	}
	
	/**
	 * Gets if this event has been {@link #consume() consumed}.
	 *
	 * @return true if this event has been consumed
	 */
	public boolean isConsumed() {
		return handled;
	}
	
	/**
	 * Return the value of the event without consuming it.
	 *
	 * @return the event's value
	 */
	public T peek() {
		return value;
	}
	
	/**
	 * Consume the event and return the value. After calling, {@link #isConsumed()} will return true.
	 *
	 * @return the event's value
	 */
	public T consume() {
		handled = true;
		return value;
	}
}
