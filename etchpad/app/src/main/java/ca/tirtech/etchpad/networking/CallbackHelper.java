package ca.tirtech.etchpad.networking;

import androidx.core.util.Consumer;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.PayloadCallback;

import java.util.function.BiConsumer;

public class CallbackHelper {
	
	public PayloadCallback payloadCallback;
	public Consumer<String> onDisconnectCallback;
	public BiConsumer<String, ConnectionResolution> onConnectionResultCallback;
	public Consumer<AckedConnectionLifecycleCallback.PendingConnection> connectionCheckCallback;
	public BiConsumer<String, ConnectionResolution> onConnectedCallback;
	public Consumer<String> connectionRejectedCallback;
	
	public CallbackHelper() {
	}
}
