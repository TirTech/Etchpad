package ca.tirtech.etchpad.mvvm;

/**
 * Functional interface for lambdas without parameters or results.
 */
@FunctionalInterface
public interface VoidFunction {
	/**
	 * Invokes the function.
	 */
	void call();
}
