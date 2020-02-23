package ca.tirtech.etchpad.networking;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import com.google.android.gms.nearby.connection.*;

import java.util.function.BiConsumer;

public class AckedConnectionLifecycleCallback extends ConnectionLifecycleCallback {
	
	private static String TAG = "AckedCLC";
	private ConnectionsClient client;
	private PayloadCallback payloadCallback;
	private Consumer<String> onDisconnectCallback;
	private BiConsumer<String, ConnectionResolution> onConnectionResultCallback;
	private Consumer<PendingConnection> connectionCheckCallback;
	
	public AckedConnectionLifecycleCallback(ConnectionsClient client) {
		this.client = client;
	}
	
	public void setOnDisconnectCallback(Consumer<String> onDisconnectCallback) {
		this.onDisconnectCallback = onDisconnectCallback;
	}
	
	public void setOnConnectionResultCallback(BiConsumer<String, ConnectionResolution> onConnectionResultCallback) {
		this.onConnectionResultCallback = onConnectionResultCallback;
	}
	
	public void setPayloadCallback(PayloadCallback payloadCallback) {
		this.payloadCallback = payloadCallback;
	}
	
	public void setConnectionCheckCallback(Consumer<PendingConnection> connectionCheckCallback) {
		this.connectionCheckCallback = connectionCheckCallback;
	}
	
	@Override
	public void onConnectionInitiated(@NonNull String endpointId, @NonNull ConnectionInfo connectionInfo) {
		connectionCheckCallback.accept(new PendingConnection(endpointId, connectionInfo));
	}
	
	@Override
	public void onConnectionResult(@NonNull String endpointId, ConnectionResolution result) {
		onConnectionResultCallback.accept(endpointId, result);
	}
	
	@Override
	public void onDisconnected(@NonNull String endpointId) {
		onDisconnectCallback.accept(endpointId);
	}
	
	public class PendingConnection {
		
		public String endpointId;
		public ConnectionInfo connectionInfo;
		
		public PendingConnection(String endpointId, ConnectionInfo connectionInfo) {
			this.endpointId = endpointId;
			this.connectionInfo = connectionInfo;
		}
		
		public void success() {
			client.acceptConnection(endpointId, new PayloadCallback() {
				@Override
				public void onPayloadReceived(@NonNull String s, @NonNull Payload payload) {
					if (payloadCallback != null) payloadCallback.onPayloadReceived(s, payload);
				}
				
				@Override
				public void onPayloadTransferUpdate(@NonNull String s, @NonNull PayloadTransferUpdate payloadTransferUpdate) {
					if (payloadCallback != null) payloadCallback.onPayloadTransferUpdate(s, payloadTransferUpdate);
				}
			});
			Log.i(TAG, "Connection Initiated on " + endpointId);
			client.stopAdvertising();
			client.stopDiscovery();
		}
		
		public void failed() {
			client.rejectConnection(endpointId);
			Log.i(TAG, "Connection Rejected on " + endpointId);
		}
	}
}
