package ca.tirtech.etchpad.hardware;

/**
 * Exception denoting a class was not initialized before being used.
 */
public class NotInitializedException extends RuntimeException {
	/**
	 * Creates exception with no extra data.
	 */
	public NotInitializedException() {
		super();
	}
	
	/**
	 * Creates exception with an error message.
	 *
	 * @param message error message
	 */
	public NotInitializedException(String message) {
		super(message);
	}
	
	/**
	 * Creates exception with an error message and root cause.
	 *
	 * @param message error message
	 * @param cause   root cause
	 */
	public NotInitializedException(String message, Throwable cause) {
		super(message, cause);
	}
}
