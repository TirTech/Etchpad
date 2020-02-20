package ca.tirtech.etchpad.drawingView.network;

import android.graphics.Canvas;
import ca.tirtech.etchpad.drawingView.DrawingLayer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NetworkedDrawingLayer extends DrawingLayer {
	
	private static final String ACTION_CLEAR = "CLEAR";
	private static final String ACTION_LINE_TO = "LINETO";
	private static final String ACTION_SET_COLOR = "SETCOLOR";
	private static final String ACTION_UNDO = "UNDO";
	
	private DrawingProtocol protocol;
	private DrawingLayer networkedLayer;
	
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
