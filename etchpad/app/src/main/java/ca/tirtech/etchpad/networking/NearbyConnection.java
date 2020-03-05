package ca.tirtech.etchpad.networking;

import android.app.Activity;
import android.util.Log;
import androidx.annotation.NonNull;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.*;
import com.google.android.gms.tasks.Task;

/**
 * Class for building and managing connections to other devices over the network, bluetooth, or NFC.
 * This class uses the NearbyConnections api to perform advertising and discovery without an intermediary
 * server.
 */
public class NearbyConnection {
	
	private static final String SERVICE_ID = "ca.tirtech.etchpad";
	private static final String TAG = "NearbyConnection";
	
	/**
	 * The currently open endpoint.
	 */
	private String activeEndpoint;
	private AckedConnectionLifecycleCallback connectionLifecycleCallback;
	private ConnectionsClient client;
	private CallbackHelper callbacks;
	
	/**
	 * Task for the last message sent. Used to ensure the connection is not closed
	 * with pending messages
	 */
	private Task<Void> currentMessage = null;
	
	/**
	 * Construct a new connection.
	 *
	 * @param activity the activity to use for the connection client
	 */
	public NearbyConnection(Activity activity) {
		client = Nearby.getConnectionsClient(activity);
		callbacks = new CallbackHelper();
		connectionLifecycleCallback = new AckedConnectionLifecycleCallback(client, callbacks);
		callbacks.onConnectionResultCallback = this::onConnectionResult;
		callbacks.onDisconnectCallback = this::onConnectionDisconnect;
	}
	
	/**
	 * Get the callback helper used to act on lifecycles.
	 *
	 * @return the callback helper
	 */
	public CallbackHelper getCallbacks() {
		return callbacks;
	}
	
	/**
	 * Begin advertising this device, processing the connection requests through this classes callback structure.
	 */
	public void advertise() {
		client.startAdvertising("Advertiser", SERVICE_ID, connectionLifecycleCallback,
				new AdvertisingOptions.Builder().setStrategy(Strategy.P2P_POINT_TO_POINT).build())
				.addOnSuccessListener(unused -> Log.i(TAG, "Advertising Initiated on " + SERVICE_ID))
				.addOnFailureListener(e -> Log.i(TAG, "Advertising Failed on " + SERVICE_ID));
	}
	
	/**
	 * Start discovering advertisers, requesting the first connection that is found.
	 */
	public void discover() {
		EndpointDiscoveryCallback endpointDiscoveryCallback = new EndpointDiscoveryCallback() {
			@Override
			public void onEndpointFound(@NonNull String endpointId, @NonNull DiscoveredEndpointInfo info) {
				client.requestConnection("Discoverer", endpointId, connectionLifecycleCallback)
						.addOnSuccessListener(unused -> Log.i(TAG, "Connected to the discovered endpoint " + endpointId))
						.addOnFailureListener(e -> {
							Log.e(TAG, "Could not connect to the discovered endpoint " + endpointId + "\n " +
									"Error was: " + e.getMessage());
							if (e.getMessage().contains("STATUS_BLUETOOTH_ERROR")) {
								client.stopDiscovery();
								disconnect();
							}
						});
			}
			
			@Override
			public void onEndpointLost(@NonNull String endpointId) {
				Log.i(TAG, "Lost endpoint " + endpointId);
			}
		};
		
		client.startDiscovery(SERVICE_ID, endpointDiscoveryCallback,
				new DiscoveryOptions.Builder().setStrategy(Strategy.P2P_POINT_TO_POINT).build())
				.addOnSuccessListener(unused -> Log.i(TAG, "Discovering Initiated on " + SERVICE_ID))
				.addOnFailureListener(e -> Log.i(TAG, "Discovering Failed on " + SERVICE_ID));
	}
	
	/**
	 * Send a message to the active endpoint for this connection.
	 *
	 * @param message the message to send
	 */
	public void sendMessage(String message) {
		if (isConnected()) currentMessage = client.sendPayload(activeEndpoint, Payload.fromBytes(message.getBytes()));
	}
	
	/**
	 * Get whether this connection has been established.
	 *
	 * @return whether the connection is active
	 */
	public boolean isConnected() {
		return activeEndpoint != null;
	}
	
	/**
	 * Disconnection from any active connections and stop advertising/discovering.
	 */
	public void disconnect() {
		if (isConnected()) {
			if (currentMessage != null && !currentMessage.isComplete()) {
				currentMessage.addOnCompleteListener((v) -> client.disconnectFromEndpoint(activeEndpoint));
				Log.i(TAG, "Disconnect delayed due to pending message");
			} else {
				client.disconnectFromEndpoint(activeEndpoint);
			}
			Log.i(TAG, "Disconnecting");
		}
		client.stopDiscovery();
		client.stopAdvertising();
	}
	
	/**
	 * Called when the connection status is resolved after being requested. The {@code onConnectedCallback} or
	 * {@code connectionRejectedCallback} will be called based on the connection state.
	 *
	 * @param endpointId the endpoint of the connection
	 * @param result     the result of the connection request
	 */
	private void onConnectionResult(@NonNull String endpointId, ConnectionResolution result) {
		if (result.getStatus().getStatusCode() == ConnectionsStatusCodes.STATUS_OK) {
			Log.i(TAG, "Connection OK on " + endpointId + ".");
			activeEndpoint = endpointId;
			if (callbacks.onConnectedCallback != null) callbacks.onConnectedCallback.accept(endpointId, result);
		} else if (result.getStatus().getStatusCode() == ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED) {
			callbacks.connectionRejectedCallback.accept(endpointId);
		}
	}
	
	/**
	 * Called when the connection is broken.
	 *
	 * @param endpointId the id of the endpoint broken
	 */
	private void onConnectionDisconnect(String endpointId) {
		Log.i(TAG, "Connection disconnected on " + endpointId);
		activeEndpoint = null;
	}
}
