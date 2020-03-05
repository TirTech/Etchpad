package ca.tirtech.etchpad.drawingView.network;

import android.app.Activity;
import android.content.Context;
import android.location.LocationManager;
import android.util.Log;
import androidx.annotation.NonNull;
import ca.tirtech.etchpad.R;
import ca.tirtech.etchpad.drawingView.DrawingLayer;
import ca.tirtech.etchpad.drawingView.DrawingModel;
import ca.tirtech.etchpad.networking.CallbackHelper;
import ca.tirtech.etchpad.networking.NearbyConnection;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class for managing device-to-device communication using {@link NearbyConnection}. This class defines a standard
 * set of messages accepted by this connection, as well as callbacks and {@link DrawingLayer} integration for
 * propagating changes between devices.
 */
public class DrawingProtocol {
	
	private static final int WAITING = 1;
	private static final int CONFIRM_PROMPT = 2;
	private static final int PROMPT_NICKNAME = 3;
	private static final int CONFIRM_WAIT = 4;
	private static final int SYNC_START = 5;
	private static final int SYNC_SEND = 6;
	private static final int SYNC_DONE = 7;
	private static final int MAX_STAGE = 8;
	
	private static final String TAG = "Drawing Protocol";
	private JSONObject newModel = null;
	private boolean host = false;
	private final NearbyConnection connection;
	private final CallbackHelper callbacks;
	private final DrawingModel model;
	private DrawingSyncDialog dialog;
	private final Activity activity;
	private final LocationManager lm;
	private String nickname;
	
	/**
	 * Create a drawing protocol to provide syncing with the given model.
	 *
	 * @param activity the activity to use for dialogs
	 * @param model    the model to sync
	 */
	public DrawingProtocol(Activity activity, DrawingModel model) {
		this.model = model;
		this.activity = activity;
		lm = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
		this.connection = new NearbyConnection(activity);
		callbacks = connection.getCallbacks();
	}
	
	/**
	 * Helper method to convert network messages back to JsonObjects.
	 *
	 * @param payload the payload to convert
	 * @return message as JSON
	 * @throws JSONException JSON was invalid
	 */
	private static JSONObject toJson(Payload payload) throws JSONException {
		if (payload == null) return new JSONObject();
		byte[] bytes = payload.asBytes();
		if (bytes == null) return new JSONObject();
		else return new JSONObject(new String(bytes));
	}
	
	/**
	 * Check that location is enabled. Required for Nearby Connections
	 *
	 * @return whether location is enabled
	 */
	private boolean isLocationEnabled() {
		return lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
	}
	
	/**
	 * Start hosting this canvas, allowing for others to connect and collaborate.
	 * Starts network advertising.
	 */
	public void host() {
		this.host = true;
		startCommunication(R.string.sync_dialog_host_wait, R.string.action_host, R.string.snack_host_cancelled);
	}
	
	/**
	 * Join a hosted canvas. Starts network discovery.
	 */
	public void join() {
		this.host = false;
		startCommunication(R.string.sync_dialog_join_wait, R.string.action_join, R.string.snack_join_cancelled);
	}
	
	/**
	 * Initialize connection callbacks and start protocol execution.
	 *
	 * @param waitingMessageId the message to show while waiting for the other device
	 * @param titleId          the title of the dialog
	 * @param cancelId         the message to show on the snackbar when waiting is cancelled
	 */
	private void startCommunication(int waitingMessageId, int titleId, int cancelId) {
		if (!isLocationEnabled()) {
			model.sendSnackbarMessage(R.string.snack_no_location);
			return;
		}
		dialog = new DrawingSyncDialog(activity, titleId, MAX_STAGE);
		dialog.setStatusWithCancel(WAITING, activity.getString(waitingMessageId), (v) -> {
			dialog.close();
			connection.disconnect();
			model.sendSnackbarMessage(cancelId);
		});
		callbacks.connectionRejectedCallback = (e -> {
			dialog.close();
			connection.disconnect();
			model.sendSnackbarMessage(R.string.sync_dialog_verify_failed);
		});
		callbacks.onConnectedCallback = ((eid, cr) -> {
			setupMessageHandler();
			if (host) {
				try {
					synchronizeCanvases();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
		callbacks.connectionCheckCallback = (p ->
				dialog.promptForConfirmation(CONFIRM_PROMPT, "Verification Code: " + p.connectionInfo.getAuthenticationToken(), r -> {
					if (r) {
						dialog.promptForValue(PROMPT_NICKNAME, "Choose a nickname to display", v -> {
							this.nickname = v;
							dialog.setStatus(CONFIRM_WAIT, "Waiting for confirmation...");
							p.success();
						});
					} else {
						p.failed();
					}
				}));
		if (host) connection.advertise();
		else connection.discover();
	}
	
	/**
	 * Disconnect from any remote clients. Also stops
	 * advertising and discovery
	 */
	public void disconnect() {
		disconnect(false);
	}
	
	/**
	 * Disconnect from any remote clients. Also stops
	 * advertising and discovery
	 *
	 * @param wasMessage whether this is being called by the user or a network message
	 */
	private void disconnect(boolean wasMessage) {
		DrawingLayer layer = model.getLayer().getValue();
		if (!wasMessage) {
			try {
				JSONObject newPayload = new JSONObject();
				newPayload.put("command", "disconnect");
				connection.sendMessage(newPayload.toString());
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		if (layer instanceof NetworkedDrawingLayer) {
			try {
				model.getLayer().setValue(new DrawingLayer(layer.jsonify()));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		connection.disconnect();
		int messageId = wasMessage ? R.string.snack_disconnected_remote : R.string.snack_disconnected;
		model.sendSnackbarMessage(messageId);
	}
	
	/**
	 * Set the connection callback to a handler defining the protocol to follow.
	 */
	private void setupMessageHandler() {
		callbacks.payloadCallback = new PayloadCallback() {
			@Override
			public void onPayloadReceived(@NonNull String s, @NonNull Payload payload) {
				try {
					JSONObject data = DrawingProtocol.toJson(payload);
					String command = data.getString("command");
					
					switch (command) {
						case "sync_start":
							Log.i(TAG, "Got start sync. Sending model...");
							syncStart();
							break;
						case "sync_foreign":
							Log.i(TAG, "Got foreign model. Sending model...");
							syncForeign(data);
							break;
						case "sync_host":
							Log.i(TAG, "Got host model. Completing sync...");
							syncHost(data);
							break;
						case "sync_complete":
							Log.i(TAG, "Got sync complete. Done");
							syncComplete();
							break;
						case "perform_change":
							performChange(data);
							break;
						case "disconnect":
							disconnect(true);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			
			@Override
			public void onPayloadTransferUpdate(@NonNull String s, @NonNull PayloadTransferUpdate payloadTransferUpdate) {
			
			}
		};
	}
	
	/**
	 * Perform a synchronization between this canvas and a remote canvas.
	 *
	 * @throws JSONException JSON was invalid in a message
	 */
	public void synchronizeCanvases() throws JSONException {
		dialog.setStatus(SYNC_START, "Starting sync...");
		Log.i(TAG, "Starting Sync...");
		JSONObject newPayload = new JSONObject();
		newPayload.put("command", "sync_start");
		connection.sendMessage(newPayload.toString());
	}
	
	/**
	 * Synchronization finished. Acknowledge if we are the host. Will set up model drawing layer with a
	 * {@link NetworkedDrawingLayer} to facilitate future updates.
	 *
	 * @throws JSONException JSON was invalid
	 */
	private void syncComplete() throws JSONException {
		//Both done here
		dialog.setStatus(SYNC_DONE, "Sync Complete!");
		if (host) {
			JSONObject newPayload = new JSONObject();
			newPayload.put("command", "sync_complete");
			connection.sendMessage(newPayload.toString());
		}
		model.getLayer().setValue(new NetworkedDrawingLayer(this, model.getLayer().getValue(), newModel));
		model.getLayer().getValue().setNickname(nickname);
		dialog.close();
		model.sendSnackbarMessage(R.string.snack_connected);
	}
	
	/**
	 * Called when this connection is not the host. Responds to complete the sync.
	 *
	 * @param data the message containing the host's canvas
	 * @throws JSONException JSON was invalid
	 */
	private void syncHost(JSONObject data) throws JSONException {
		dialog.setStatus(SYNC_DONE, "Completing Sync...");
		JSONObject modelDataForeign = data.getJSONObject("data");
		JSONObject newPayload = new JSONObject();
		newPayload.put("command", "sync_complete");
		connection.sendMessage(newPayload.toString());
		newModel = modelDataForeign;
	}
	
	/**
	 * Called when this connection is the host. Responds by sending the host's canvas.
	 *
	 * @param data the message containing the remote's canvas
	 * @throws JSONException JSON was invalid
	 */
	private void syncForeign(JSONObject data) throws JSONException {
		dialog.setStatus(SYNC_SEND, "Sending Canvas...");
		JSONObject modelDataForeign = data.getJSONObject("data");
		
		JSONObject modelData = model.getLayer().getValue().jsonify();
		JSONObject newPayload = new JSONObject();
		newPayload.put("command", "sync_host");
		newPayload.put("data", modelData);
		connection.sendMessage(newPayload.toString());
		
		newModel = modelDataForeign;
	}
	
	/**
	 * Called on the remote. Host acknowledges it is ready to sync. Respond by sending remote's canvas.
	 *
	 * @throws JSONException JSON was invalid
	 */
	private void syncStart() throws JSONException {
		dialog.setStatus(SYNC_SEND, "Sending Canvas...");
		JSONObject modelData = model.getLayer().getValue().jsonify();
		JSONObject newPayload = new JSONObject();
		newPayload.put("command", "sync_foreign");
		newPayload.put("data", modelData);
		connection.sendMessage(newPayload.toString());
	}
	
	/**
	 * Request was received to update our copy of the remote canvas. Unpack and update the layer
	 *
	 * @param data JSON containing the changes
	 * @throws JSONException JSON was invalid
	 */
	private void performChange(JSONObject data) throws JSONException {
		JSONObject changes = data.getJSONObject("data");
		String action = changes.getString("action");
		JSONArray values = changes.getJSONArray("value");
		DrawingLayer layer = model.getLayer().getValue();
		if (layer instanceof NetworkedDrawingLayer) {
			((NetworkedDrawingLayer) layer).perfomNetworkAction(action, values);
		}
	}
	
	/**
	 * Local drawing layer was updated. Send message to the other device to have it update.
	 *
	 * @param action the change that occurred
	 * @param value  data about the change
	 */
	public void createNetworkAction(String action, JSONArray value) {
		try {
			if (value == null) {
				value = new JSONArray();
			}
			JSONObject json = new JSONObject();
			JSONObject data = new JSONObject();
			data.put("action", action);
			data.put("value", value);
			json.put("command", "perform_change");
			json.put("data", data);
			connection.sendMessage(json.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
