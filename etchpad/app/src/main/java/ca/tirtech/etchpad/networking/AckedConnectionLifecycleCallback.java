package ca.tirtech.etchpad.networking;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import com.google.android.gms.nearby.connection.*;

import java.util.function.BiConsumer;

/**
 * Class for hosting callbacks and managing the connection lifecycle through request, approval/rejection,
 * establishment, and disconnect.
 */
public class AckedConnectionLifecycleCallback extends ConnectionLifecycleCallback {
	
	private static String TAG = "AckedCLC";
	private ConnectionsClient client;
	private PayloadCallback payloadCallback;
	private Consumer<String> onDisconnectCallback;
	private BiConsumer<String, ConnectionResolution> onConnectionResultCallback;
	private Consumer<PendingConnection> connectionCheckCallback;
	
	/**
	 * Create a new instance for the given ConnectionClient.
	 *
	 * @param client the client to use
	 */
	public AckedConnectionLifecycleCallback(ConnectionsClient client) {
		this.client = client;
	}
	
	/**
	 * Set the callback to invoke when the connection is disconnected.
	 *
	 * @param onDisconnectCallback
	 */
	public void setOnDisconnectCallback(Consumer<String> onDisconnectCallback) {
		this.onDisconnectCallback = onDisconnectCallback;
	}
	
	
	public void setOnConnectionResultCallback(BiConsumer<String, ConnectionResolution> onConnectionResultCallback) {
		this.onConnectionResultCallback = onConnectionResultCallback;
	}
	
	/**
	 * Set the callback to invoke when messages are sent to this device over an active connection.
	 *
	 * @param payloadCallback the callback to set
	 */
	public void setPayloadCallback(PayloadCallback payloadCallback) {
		this.payloadCallback = payloadCallback;
	}
	
	/**
	 * Set the callback to invoke to verify the connection is allowed before completing.
	 *
	 * @param connectionCheckCallback the callback to set
	 */
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
	
	/**
	 * Helper class containing the information for a new connection request. Contains success and fail methods for
	 * allowing and denying the requested connection.
	 */
	public class PendingConnection {
		
		/**
		 * The endpoint being requested.
		 */
		public String endpointId;
		
		/**
		 * Details about the connection.
		 */
		public ConnectionInfo connectionInfo;
		
		/**
		 * Create a new connection request.
		 *
		 * @param endpointId     endpoint being requested
		 * @param connectionInfo connection information
		 */
		public PendingConnection(String endpointId, ConnectionInfo connectionInfo) {
			this.endpointId = endpointId;
			this.connectionInfo = connectionInfo;
		}
		
		/**
		 * Call to accept the connection.
		 */
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
		
		/**
		 * Call to reject the connection.
		 */
		public void failed() {
			client.rejectConnection(endpointId);
			Log.i(TAG, "Connection Rejected on " + endpointId);
		}
	}
}
