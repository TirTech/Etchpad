package ca.tirtech.etchpad.drawingView.network;

import android.app.Activity;
import android.util.Log;
import androidx.annotation.NonNull;
import ca.tirtech.etchpad.drawingView.DrawingLayer;
import ca.tirtech.etchpad.drawingView.DrawingModel;
import ca.tirtech.etchpad.hardware.NearbyConnection;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DrawingProtocol {
	
	private static String TAG = "Drawing Protocol";
	private JSONObject newModel = null;
	private boolean host = false;
	private NearbyConnection connection;
	private DrawingModel model;
	private DrawingSyncDialog dialog;
	private Activity activity;
	
	public DrawingProtocol(Activity activity, DrawingModel model) {
		this.model = model;
		this.activity = activity;
		this.connection = new NearbyConnection(activity);
	}
	
	private static JSONObject toJson(Payload payload) throws JSONException {
		return new JSONObject(new String(payload.asBytes()));
	}
	
	public void host() {
		this.host = true;
		dialog = new DrawingSyncDialog(activity);
		dialog.setStatus(1, "Waiting for client...");
		connection.setOnConnected((eid, cr) -> {
			try {
				synchronizeCanvases();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		});
		connection.advertise();
	}
	
	public void join() {
		this.host = false;
		dialog = new DrawingSyncDialog(activity);
		dialog.setStatus(1, "Searching for host...");
		connection.setOnConnected((eid, cr) -> {
			try {
				synchronizeCanvases();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		});
		connection.discover();
	}
	
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
	}
	
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
							syncComplete(data);
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
	
	private void syncComplete(JSONObject data) throws JSONException {
		//Both done here
		dialog.setStatus(5, "Sync Complete!");
		if (host) {
			JSONObject newPayload = new JSONObject();
			newPayload.put("command", "sync_complete");
			connection.sendMessage(newPayload.toString());
		}
		model.getLayer().setValue(new NetworkedDrawingLayer(this, model.getLayer().getValue(), newModel));
		dialog.close();
	}
	
	private void syncHost(JSONObject data) throws JSONException {
		dialog.setStatus(4, "Sending Canvas...");
		JSONObject modelDataForeign = data.getJSONObject("data");
		JSONObject newPayload = new JSONObject();
		newPayload.put("command", "sync_complete");
		connection.sendMessage(newPayload.toString());
		newModel = modelDataForeign;
	}
	
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
	
	private void syncStart() throws JSONException {
		dialog.setStatus(3, "Sending Canvas...");
		JSONObject modelData = model.getLayer().getValue().jsonify();
		JSONObject newPayload = new JSONObject();
		newPayload.put("command", "sync_foreign");
		newPayload.put("data", modelData);
		connection.sendMessage(newPayload.toString());
	}
	
	private void performChange(JSONObject data) throws JSONException {
		JSONObject changes = data.getJSONObject("data");
		String action = changes.getString("action");
		JSONArray values = changes.getJSONArray("value");
		DrawingLayer layer = model.getLayer().getValue();
		if (layer instanceof NetworkedDrawingLayer) {
			((NetworkedDrawingLayer) layer).perfomNetworkAction(action, values);
		}
	}
	
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
