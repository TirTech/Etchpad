package ca.tirtech.etchpad.drawingView.network;

import android.app.Activity;
import android.util.Log;
import androidx.annotation.NonNull;
import ca.tirtech.etchpad.R;
import ca.tirtech.etchpad.drawingView.DrawingLayer;
import ca.tirtech.etchpad.drawingView.DrawingModel;
import ca.tirtech.etchpad.hardware.NearbyConnection;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DrawingProtocol {
	
	private static final String TAG = "Drawing Protocol";
	private JSONObject newModel = null;
	private boolean host = false;
	private NearbyConnection connection;
	private DrawingModel model;
	private DrawingSyncDialog dialog;
	private Activity activity;
	
	/**
	 * Create a drawing protocol to provide syncing with the given model.
	 *
	 * @param activity the activity to use for dialogs
	 * @param model    the model to sync
	 */
	public DrawingProtocol(Activity activity, DrawingModel model) {
		this.model = model;
		this.activity = activity;
		this.connection = new NearbyConnection(activity);
	}
	
	/**
	 * Helper method to convert network messages back to JsonObjects
	 *
	 * @param payload the payload to convert
	 * @return message as JSON
	 * @throws JSONException JSON was invalid
	 */
	private static JSONObject toJson(Payload payload) throws JSONException {
		return new JSONObject(new String(payload.asBytes()));
	}
	
	/**
	 * Start hosting this canvas, allowing for others to connect and collaborate.
	 * Starts network advertising.
	 */
	public void host() {
		this.host = true;
		dialog = new DrawingSyncDialog(activity);
		dialog.setStatusWithCancel(1, "Waiting for client...", (v) -> {
			dialog.close();
			connection.disconnect();
			Snackbar.make(activity.findViewById(R.id.activity_main), R.string.snack_host_cancelled, BaseTransientBottomBar.LENGTH_SHORT).show();
		});
		connection.setOnConnected((eid, cr) -> {
			try {
				synchronizeCanvases();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		});
		connection.advertise();
	}
	
	/**
	 * Join a hosted canvas. Starts network discovery.
	 */
	public void join() {
		this.host = false;
		dialog = new DrawingSyncDialog(activity);
		dialog.setStatusWithCancel(1, "Searching for host...", (v) -> {
			dialog.close();
			connection.disconnect();
			Snackbar.make(activity.findViewById(R.id.activity_main), R.string.snack_join_cancelled, BaseTransientBottomBar.LENGTH_SHORT).show();
		});
		connection.setOnConnected((eid, cr) -> {
			try {
				synchronizeCanvases();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		});
		connection.discover();
	}
	
	/**
	 * Disconnect from any remote clients. Also stops
	 * advertising and discovery
	 */
	public void disconnect() {
		DrawingLayer layer = model.getLayer().getValue();
		if (layer instanceof NetworkedDrawingLayer) {
			try {
				model.getLayer().setValue(new DrawingLayer(layer.jsonify()));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		connection.disconnect();
		Snackbar.make(activity.findViewById(R.id.activity_main), R.string.snack_disconnected, BaseTransientBottomBar.LENGTH_SHORT).show();
	}
	
	/**
	 * Perform a synchronization between this canvas and a remote canvas, following a message protocol.
	 *
	 * @throws JSONException JSON was invalid in a message
	 */
	public void synchronizeCanvases() throws JSONException {
		
		PayloadCallback callback = new PayloadCallback() {
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
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			
			@Override
			public void onPayloadTransferUpdate(@NonNull String s, @NonNull PayloadTransferUpdate payloadTransferUpdate) {
			
			}
		};
		
		connection.setPayloadCallback(callback);
		
		if (host) {
			dialog.setStatus(3, "Starting sync...");
			Log.i(TAG, "Starting Sync...");
			JSONObject newPayload = new JSONObject();
			newPayload.put("command", "sync_start");
			connection.sendMessage(newPayload.toString());
		}
	}
	
	/**
	 * Synchronization finished. Acknowledge if we are the host. Will set up model drawing layer with a
	 * {@link NetworkedDrawingLayer} to facilitate future updates.
	 *
	 * @throws JSONException JSON was invalid
	 */
	private void syncComplete() throws JSONException {
		//Both done here
		dialog.setStatus(5, "Sync Complete!");
		if (host) {
			JSONObject newPayload = new JSONObject();
			newPayload.put("command", "sync_complete");
			connection.sendMessage(newPayload.toString());
		}
		model.getLayer().setValue(new NetworkedDrawingLayer(this, model.getLayer().getValue(), newModel));
		dialog.close();
		Snackbar.make(activity.findViewById(R.id.activity_main), R.string.snack_connected, BaseTransientBottomBar.LENGTH_SHORT).show();
	}
	
	/**
	 * Called when this connection is not the host. Responds to complete the sync.
	 *
	 * @param data the message containing the host's canvas
	 * @throws JSONException JSON was invalid
	 */
	private void syncHost(JSONObject data) throws JSONException {
		dialog.setStatus(4, "Sending Canvas...");
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
		dialog.setStatus(4, "Sending Canvas...");
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
		dialog.setStatus(3, "Sending Canvas...");
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
