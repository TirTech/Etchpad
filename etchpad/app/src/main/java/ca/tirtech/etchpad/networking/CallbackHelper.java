package ca.tirtech.etchpad.networking;

import androidx.core.util.Consumer;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.PayloadCallback;

import java.util.function.BiConsumer;

/**
 * Assists in providing callbacks for device to device communications. This is a container class.
 */
public class CallbackHelper {
	
	/**
	 * Callback used for received messages.
	 */
	public PayloadCallback payloadCallback;
	/**
	 * Callback used when connection is disconnected (regularly and abnormally).
	 */
	public Consumer<String> onDisconnectCallback;
	/**
	 * Callback used when connection requests have been serviced and either accepted or rejected.
	 */
	public BiConsumer<String, ConnectionResolution> onConnectionResultCallback;
	/**
	 * Callback used to verify whether a requested connection is allowed (i.e. verification code check, ui updates, etc.)
	 */
	public Consumer<AckedConnectionLifecycleCallback.PendingConnection> connectionCheckCallback;
	/**
	 * Callback used when a connection has been established successfully.
	 */
	public BiConsumer<String, ConnectionResolution> onConnectedCallback;
	/**
	 * Callback used when a connection has been rejected (usually via {@link #connectionCheckCallback}).
	 */
	public Consumer<String> connectionRejectedCallback;
	
	/**
	 * Create a new blank helper.
	 */
	public CallbackHelper() {
	}
}
