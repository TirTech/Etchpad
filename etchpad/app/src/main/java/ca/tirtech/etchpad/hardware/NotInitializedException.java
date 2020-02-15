package ca.tirtech.etchpad.hardware;

public class NotInitializedException extends RuntimeException {
	public NotInitializedException() {
		super();
	}
	
	public NotInitializedException(String message) {
		super(message);
	}
	
	public NotInitializedException(String message, Throwable cause) {
		super(message, cause);
	}
}
