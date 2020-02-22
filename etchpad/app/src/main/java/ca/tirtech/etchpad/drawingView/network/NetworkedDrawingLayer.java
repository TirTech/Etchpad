package ca.tirtech.etchpad.drawingView.network;

import android.graphics.Canvas;
import ca.tirtech.etchpad.drawingView.DrawingLayer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Extension of the drawing layer allowing for changes to the layer to be propagated
 * over the network to a connected device. This class wraps all functions provided by
 * {@link DrawingLayer} that need to be synchronized between devices.
 * <p/>
 * This class also hosts a copy of the layer drawn on by the remote device. This class
 * will additionally draw the remote layer whenever it is asked to draw itself. This
 * class receives changes in the remote layer via {@link #perfomNetworkAction(String, JSONArray)}
 * and applies these to the networked layer. This class is not responsible for the maintenance
 * of the connection. See {@link DrawingProtocol} for the networking implementation.
 */
public class NetworkedDrawingLayer extends DrawingLayer {
	
	private static final String ACTION_CLEAR = "CLEAR";
	private static final String ACTION_LINE_TO = "LINETO";
	private static final String ACTION_SET_COLOR = "SETCOLOR";
	private static final String ACTION_UNDO = "UNDO";
	
	private DrawingProtocol protocol;
	private DrawingLayer networkedLayer;
	
	/**
	 * Constructs an instance, initializing itself to a clone of the provided layer. Also takes the remote layer
	 * to be drawn on from the network.
	 *
	 * @param protocol       the network connection
	 * @param layer          the layer to duplicate
	 * @param networkedLayer the layer from the remote device
	 * @throws JSONException JSON was invalid
	 */
	public NetworkedDrawingLayer(DrawingProtocol protocol, DrawingLayer layer, JSONObject networkedLayer) throws JSONException {
		super(layer);
		this.protocol = protocol;
		this.networkedLayer = new DrawingLayer(networkedLayer);
	}
	
	@Override
	public void clear() {
		super.clear();
		protocol.createNetworkAction(ACTION_CLEAR, null);
	}
	
	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);
		networkedLayer.draw(canvas);
	}
	
	@Override
	public void lineTo(float x, float y) {
		super.lineTo(x, y);
		try {
			JSONArray json = new JSONArray();
			json.put(x);
			json.put(y);
			protocol.createNetworkAction(ACTION_LINE_TO, json);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void setColor(int color) {
		super.setColor(color);
		JSONArray json = new JSONArray();
		json.put(color);
		protocol.createNetworkAction(ACTION_SET_COLOR, json);
	}
	
	@Override
	public void undo() {
		super.undo();
		protocol.createNetworkAction(ACTION_UNDO, null);
	}
	
	/**
	 * Handle a received network message with a specified action and data. Actions are
	 * performed against the {@link #networkedLayer};
	 *
	 * @param action the action to perform
	 * @param value  the data required to perform the action (may be empty)
	 * @throws JSONException JSON was invalid
	 */
	public void perfomNetworkAction(String action, JSONArray value) throws JSONException {
		switch (action) {
			case ACTION_CLEAR:
				networkedLayer.clear();
				break;
			case ACTION_LINE_TO:
				networkedLayer.lineTo(value.getInt(0), value.getInt(1));
				break;
			case ACTION_SET_COLOR:
				networkedLayer.setColor(value.getInt(0));
				break;
			case ACTION_UNDO:
				networkedLayer.undo();
				break;
		}
		notifyChange();
	}
}
