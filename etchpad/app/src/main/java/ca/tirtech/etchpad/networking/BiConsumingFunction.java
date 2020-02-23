package ca.tirtech.etchpad.networking;

/**
 * Consumes two inputs, producing one output
 */
@FunctionalInterface
public interface BiConsumingFunction<T, U, R> {
	R apply(T t, U u);
}
