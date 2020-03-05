package ca.tirtech.etchpad.networking;

import android.util.Log;
import androidx.annotation.NonNull;
import com.google.android.gms.nearby.connection.*;

/**
 * Class for hosting callbacks and managing the connection lifecycle through request, approval/rejection,
 * establishment, and disconnect.
 */
public class AckedConnectionLifecycleCallback extends ConnectionLifecycleCallback {
	
	private static final String TAG = "AckedCLC";
	private final ConnectionsClient client;
	private final CallbackHelper callbacks;
	
	/**
	 * Create a new instance for the given ConnectionClient.
	 *
	 * @param client    the client to use
	 * @param callbacks the helper containing callbacks for this lifecycle
	 */
	public AckedConnectionLifecycleCallback(ConnectionsClient client, CallbackHelper callbacks) {
		this.client = client;
		this.callbacks = callbacks;
	}
	
	@Override
	public void onConnectionInitiated(@NonNull String endpointId, @NonNull ConnectionInfo connectionInfo) {
		callbacks.connectionCheckCallback.accept(new PendingConnection(endpointId, connectionInfo));
	}
	
	@Override
	public void onConnectionResult(@NonNull String endpointId, @NonNull ConnectionResolution result) {
		callbacks.onConnectionResultCallback.accept(endpointId, result);
	}
	
	@Override
	public void onDisconnected(@NonNull String endpointId) {
		callbacks.onDisconnectCallback.accept(endpointId);
	}
	
	/**
	 * Helper class containing the information for a new connection request. Contains success and fail methods for
	 * allowing and denying the requested connection.
	 */
	public class PendingConnection {
		
		/**
		 * The endpoint being requested.
		 */
		public final String endpointId;
		
		/**
		 * Details about the connection.
		 */
		public final ConnectionInfo connectionInfo;
		
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
					if (callbacks.payloadCallback != null) callbacks.payloadCallback.onPayloadReceived(s, payload);
				}
				
				@Override
				public void onPayloadTransferUpdate(@NonNull String s, @NonNull PayloadTransferUpdate payloadTransferUpdate) {
					if (callbacks.payloadCallback != null) callbacks.payloadCallback.onPayloadTransferUpdate(s, payloadTransferUpdate);
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
