package ca.tirtech.etchpad.hardware;

import android.app.Activity;
import android.util.Log;
import androidx.annotation.NonNull;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.*;

import java.util.function.BiConsumer;

public class NearbyConnection {
	private static final String SERVICE_ID = "ca.tirtech.testingapp";
	private static final String TAG = "NearbyConnection";
	private final PayloadCallback defaultPayloadCallback = new PayloadCallback() {
		@Override
		public void onPayloadReceived(@NonNull String s, @NonNull Payload payload) {
			byte[] data = payload.asBytes();
			Log.i(TAG, "DATA WAS " + new String(data) + " FROM " + s);
		}
		
		@Override
		public void onPayloadTransferUpdate(@NonNull String s, @NonNull PayloadTransferUpdate payloadTransferUpdate) {
			//Do Nothing
		}
	};
	private Activity activity;
	private String activeEndpoint;
	private PayloadCallback payloadCallback;
	private final PayloadCallback callbackWrapper = new PayloadCallback() {
		@Override
		public void onPayloadReceived(@NonNull String s, @NonNull Payload payload) {
			if (payloadCallback != null) {
				payloadCallback.onPayloadReceived(s, payload);
			} else {
				defaultPayloadCallback.onPayloadReceived(s, payload);
			}
		}
		
		@Override
		public void onPayloadTransferUpdate(@NonNull String s, @NonNull PayloadTransferUpdate payloadTransferUpdate) {
			if (payloadCallback != null) {
				payloadCallback.onPayloadTransferUpdate(s, payloadTransferUpdate);
			} else {
				defaultPayloadCallback.onPayloadTransferUpdate(s, payloadTransferUpdate);
			}
		}
	};
	private BiConsumer<String, ConnectionResolution> onConnected;
	private final ConnectionLifecycleCallback connectionLifecycleCallback =
			new ConnectionLifecycleCallback() {
				@Override
				public void onConnectionInitiated(@NonNull String endpointId, @NonNull ConnectionInfo connectionInfo) {
					// Automatically accept the connection on both sides.
					Nearby.getConnectionsClient(activity).acceptConnection(endpointId, callbackWrapper);
					Log.i(TAG, "Connection Initiated on " + endpointId);
					Nearby.getConnectionsClient(activity).stopAdvertising();
				}
				
				@Override
				public void onConnectionResult(@NonNull String endpointId, ConnectionResolution result) {
					switch (result.getStatus().getStatusCode()) {
						case ConnectionsStatusCodes.STATUS_OK:
							// We're connected! Can now start sending and receiving data.
							Log.i(TAG, "Connection OK on " + endpointId + ".");
							activeEndpoint = endpointId;
							if (onConnected != null) {
								onConnected.accept(endpointId, result);
							}
							break;
						case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
							// The connection was rejected by one or both sides.
							Log.e(TAG, "Connection Rejected on " + endpointId);
							break;
						case ConnectionsStatusCodes.STATUS_ERROR:
							// The connection broke before it was able to be accepted.
							Log.e(TAG, "Connection Borked on " + endpointId);
							break;
						default:
							// Unknown status code
					}
				}
				
				@Override
				public void onDisconnected(@NonNull String endpointId) {
					// We've been disconnected from this endpoint. No more data can be
					// sent or received.
					Log.i(TAG, "Connection disconnected on " + endpointId);
					activeEndpoint = null;
				}
			};
	private final EndpointDiscoveryCallback endpointDiscoveryCallback =
			new EndpointDiscoveryCallback() {
				@Override
				public void onEndpointFound(@NonNull String endpointId, @NonNull DiscoveredEndpointInfo info) {
					// An endpoint was found. We request a connection to it.
					Nearby.getConnectionsClient(activity)
							.requestConnection("Discoverer", endpointId, connectionLifecycleCallback)
							.addOnSuccessListener(
									(Void unused) -> {
										// We successfully requested a connection. Now both sides
										// must accept before the connection is established.
										Log.i(TAG, "Connected to the discovered endpoint " + endpointId);
										Nearby.getConnectionsClient(activity).stopDiscovery();
									})
							.addOnFailureListener(
									(Exception e) -> {
										// Nearby Connections failed to request the connection.
										Log.e(TAG, "Could not connect to the discovered endpoint" + endpointId);
									});
				}
				
				@Override
				public void onEndpointLost(@NonNull String endpointId) {
					// A previously discovered endpoint has gone away.
					Log.i(TAG, "Lost endpoint " + endpointId);
				}
			};
	
	
	public NearbyConnection(Activity activity) {
		this.activity = activity;
	}
	
	public PayloadCallback getPayloadCallback() {
		return payloadCallback;
	}
	
	public void setPayloadCallback(PayloadCallback payloadCallback) {
		this.payloadCallback = payloadCallback;
	}
	
	public BiConsumer<String, ConnectionResolution> getOnConnected() {
		return onConnected;
	}
	
	public void setOnConnected(BiConsumer<String, ConnectionResolution> onConnected) {
		this.onConnected = onConnected;
	}
	
	public void advertise() {
		AdvertisingOptions advertisingOptions =
				new AdvertisingOptions.Builder().setStrategy(Strategy.P2P_POINT_TO_POINT).build();
		Nearby.getConnectionsClient(activity)
				.startAdvertising("Advertiser", SERVICE_ID, connectionLifecycleCallback, advertisingOptions)
				.addOnSuccessListener(
						(Void unused) -> {
							// We're advertising!
							Log.i(TAG, "Advertising Initiated on " + SERVICE_ID);
						})
				.addOnFailureListener(
						(Exception e) -> {
							// We were unable to start advertising.
							Log.i(TAG, "Advertising Failed on " + SERVICE_ID);
						});
	}
	
	public void discover() {
		DiscoveryOptions discoveryOptions =
				new DiscoveryOptions.Builder().setStrategy(Strategy.P2P_POINT_TO_POINT).build();
		Nearby.getConnectionsClient(activity)
				.startDiscovery(SERVICE_ID, endpointDiscoveryCallback, discoveryOptions)
				.addOnSuccessListener(
						(Void unused) -> {
							// We're discovering!
							Log.i(TAG, "Discovering Initiated on " + SERVICE_ID);
						})
				.addOnFailureListener(
						(Exception e) -> {
							// We're unable to start discovering.
							Log.i(TAG, "Discovering Failed on " + SERVICE_ID);
						});
	}
	
	public void sendMessage(String message) {
		if (activeEndpoint != null) {
			Nearby.getConnectionsClient(activity).sendPayload(activeEndpoint,
					Payload.fromBytes(message.getBytes()));
		}
	}
	
	public boolean isConnected() {
		return activeEndpoint != null;
	}
	
	public void disconnect() {
		if (activeEndpoint != null) {
			Nearby.getConnectionsClient(activity).disconnectFromEndpoint(activeEndpoint);
			Log.i(TAG, "Disconnecting.");
		}
	}
	
}
