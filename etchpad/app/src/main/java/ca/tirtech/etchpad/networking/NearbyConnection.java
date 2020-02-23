package ca.tirtech.etchpad.networking;

import android.app.Activity;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.*;
import com.google.android.gms.tasks.Task;

import java.util.function.BiConsumer;

public class NearbyConnection {
	
	private static final String SERVICE_ID = "ca.tirtech.testingapp";
	private static final String TAG = "NearbyConnection";
	
	private String activeEndpoint;
	private BiConsumer<String, ConnectionResolution> onConnectedCallback;
	private AckedConnectionLifecycleCallback connectionLifecycleCallback;
	private ConnectionsClient client;
	private Task<Void> currentMessage = null;
	private Consumer<String> connectionRejectedCallback;
	
	public NearbyConnection(Activity activity) {
		client = Nearby.getConnectionsClient(activity);
		connectionLifecycleCallback = new AckedConnectionLifecycleCallback(client);
		connectionLifecycleCallback.setOnConnectionResultCallback(this::onConnectionResult);
		connectionLifecycleCallback.setOnDisconnectCallback(this::onConnectionDisconnect);
	}
	
	public void setPayloadCallback(PayloadCallback payloadCallback) {
		connectionLifecycleCallback.setPayloadCallback(payloadCallback);
	}
	
	public void setOnConnectedCallback(BiConsumer<String, ConnectionResolution> onConnectedCallback) {
		this.onConnectedCallback = onConnectedCallback;
	}
	
	public void advertise() {
		client.startAdvertising("Advertiser", SERVICE_ID, connectionLifecycleCallback,
				new AdvertisingOptions.Builder().setStrategy(Strategy.P2P_POINT_TO_POINT).build())
				.addOnSuccessListener(unused -> Log.i(TAG, "Advertising Initiated on " + SERVICE_ID))
				.addOnFailureListener(e -> Log.i(TAG, "Advertising Failed on " + SERVICE_ID));
	}
	
	public void setConnectionRejectedCallback(Consumer<String> connectionRejectedCallback) {
		this.connectionRejectedCallback = connectionRejectedCallback;
	}
	
	public void discover() {
		EndpointDiscoveryCallback endpointDiscoveryCallback = new EndpointDiscoveryCallback() {
			@Override
			public void onEndpointFound(@NonNull String endpointId, @NonNull DiscoveredEndpointInfo info) {
				client.requestConnection("Discoverer", endpointId, connectionLifecycleCallback)
						.addOnSuccessListener(unused -> Log.i(TAG, "Connected to the discovered endpoint " + endpointId))
						.addOnFailureListener(e -> Log.e(TAG, "Could not connect to the discovered endpoint" + endpointId));
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
	
	public void sendMessage(String message) {
		if (isConnected()) currentMessage = client.sendPayload(activeEndpoint, Payload.fromBytes(message.getBytes()));
	}
	
	public boolean isConnected() {
		return activeEndpoint != null;
	}
	
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
	
	private void onConnectionResult(@NonNull String endpointId, ConnectionResolution result) {
		if (result.getStatus().getStatusCode() == ConnectionsStatusCodes.STATUS_OK) {
			Log.i(TAG, "Connection OK on " + endpointId + ".");
			activeEndpoint = endpointId;
			if (onConnectedCallback != null) onConnectedCallback.accept(endpointId, result);
		} else if (result.getStatus().getStatusCode() == ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED) {
			connectionRejectedCallback.accept(endpointId);
		}
	}
	
	private void onConnectionDisconnect(String endpointId) {
		Log.i(TAG, "Connection disconnected on " + endpointId);
		activeEndpoint = null;
	}
	
	public void setConnectionCheckCallback(Consumer<AckedConnectionLifecycleCallback.PendingConnection> connectionCheckCallback) {
		connectionLifecycleCallback.setConnectionCheckCallback(connectionCheckCallback);
	}
}
