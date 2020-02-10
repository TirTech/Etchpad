package ca.tirtech.etchpad;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import androidx.core.util.Pair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Stack;

public class DrawingLayer {
	private static final String JSON_LAYER_PATHS = "layer_paths";
	private static final String JSON_PAINT_COLOR = "paint_color";
	private static final String JSON_X = "x";
	private static final String JSON_Y = "y";
	private static final String JSON_PATH = "path";
	
	private Stack<LayerPath> paths = new Stack<>();
	
	public DrawingLayer(float x, float y) {
		paths.push(new LayerPath(initPaint(Color.RED), x, y));
	}
	
	/**
	 * Creates a new drawing layer from the given JSON. This is a convenience constructor, replacing the following:<br/>
	 * <code>
	 * DrawingLayer layer = new DrawingLayer(0,0);<br/>
	 * layer.{@link #objectify(JSONObject)};
	 * </code>
	 *
	 * @param root the JSON to load from
	 */
	public DrawingLayer(JSONObject root) throws JSONException {
		objectify(root);
	}
	
	/**
	 * Create a new {@link Paint}
	 *
	 * @param color the color of the paint
	 * @return a new {@link Paint} with the specified color
	 */
	private Paint initPaint(int color) {
		Paint paint = new Paint();
		paint.setAntiAlias(false);
		paint.setColor(color);
		paint.setStrokeJoin(Paint.Join.BEVEL);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(5f);
		return paint;
	}
	
	/**
	 * Draws all elements of this layer on the provided {@link Canvas}
	 *
	 * @param canvas the canvas to draw on
	 */
	public void draw(Canvas canvas) {
		for (LayerPath lp : paths) {
			canvas.drawPath(lp.path, lp.paint);
		}
	}
	
	/**
	 * Continue the current line by (x,y) from the current position
	 *
	 * @param x the x offset to draw to
	 * @param y the y offset to draw to
	 */
	public void lineToByOffset(float x, float y) {
		LayerPath cur = getCurrentLayerPath();
		lineTo(cur.x + x, cur.y + y);
	}
	
	/**
	 * Continue the current line to the x
	 *
	 * @param x the x position to draw to
	 * @param y the y position to draw to
	 */
	public void lineTo(float x, float y) {
		LayerPath cur = getCurrentLayerPath();
		cur.path.lineTo(x, y);
		cur.pathPoints.add(new Pair<>(x, y));
		cur.x = x;
		cur.y = y;
	}
	
	/**
	 * Get the current layer path. Defined as the layer path that is on the top of the path stack
	 *
	 * @return the top-most layer path
	 */
	private LayerPath getCurrentLayerPath() {
		return paths.peek();
	}
	
	/**
	 * Convert this {@link DrawingLayer} into a {@link JSONObject}. To recreate this object, use {@link #objectify(JSONObject)}
	 *
	 * @return the JSON representation of this object
	 * @throws JSONException thrown if instance values were not valid JSON types
	 */
	public JSONObject jsonify() throws JSONException {
		JSONObject root = new JSONObject();
		JSONArray layers = new JSONArray();
		for (LayerPath p : paths) {
			layers.put(p.jsonify());
		}
		root.put(JSON_LAYER_PATHS, layers);
		return root;
	}
	
	/**
	 * Load the given {@link JSONObject} into this {@link DrawingLayer}. JSON should conform to the output of {@link #jsonify()}
	 *
	 * @param root the JSON to load from
	 * @throws JSONException thrown if loaded data was not valid JSON
	 */
	public void objectify(JSONObject root) throws JSONException {
		paths = new Stack<>();
		JSONArray layers = root.getJSONArray(JSON_LAYER_PATHS);
		for (int i = 0; i < layers.length(); i++) {
			paths.push(new LayerPath(layers.getJSONObject(i)));
		}
	}
	
	/**
	 * Get the color of the current path
	 *
	 * @return the current path's paint's color
	 */
	public int getColor() {
		return getCurrentLayerPath().paint.getColor();
	}
	
	/**
	 * Assign the color when drawing on this layer. This will create a new {@link Paint} for this color.
	 * Since each path must have its own paint, this will create a new path.
	 *
	 * @param color the color to set to
	 */
	public void setColor(int color) {
		LayerPath cur = getCurrentLayerPath();
		paths.push(new LayerPath(initPaint(color), cur.x, cur.y));
	}
	
	/**
	 * Removes the top-most path from the layer
	 */
	public void undo() {
		if (paths.size() > 1) {
			paths.pop();
		} else if (paths.size() == 1) {
			paths.set(0, new LayerPath(initPaint(Color.RED), 1000, 500));
		}
	}
	
	/**
	 * A container for settings defining a {@link Path} with a color and current position (head).
	 * Used as a substitute for {@link Pair}s of {@link Path}s and {@link Paint}s
	 */
	private class LayerPath {
		Path path;
		Paint paint;
		ArrayList<Pair<Float, Float>> pathPoints = new ArrayList<>();
		float x;
		float y;
		
		public LayerPath(Paint paint, float x, float y) {
			this.path = new Path();
			this.path.moveTo(x, y);
			this.pathPoints.add(new Pair<>(x, y));
			this.x = x;
			this.y = y;
			this.paint = paint;
		}
		
		/**
		 * Creates a new layer from the given JSON. This is a convenience constructor, replacing the following:<br/>
		 * <code>
		 * LayerPath layer = new LayerPath(Color.RED,0,0);<br/>
		 * layer.{@link #objectify(JSONObject)};
		 * </code>
		 *
		 * @param root the JSON to load from
		 */
		public LayerPath(JSONObject root) throws JSONException {
			objectify(root);
		}
		
		/**
		 * Convert this {@link LayerPath} into a {@link JSONObject}. To recreate this object, use {@link #objectify(JSONObject)}
		 *
		 * @return the JSON representation of this object
		 * @throws JSONException thrown if instance values were not valid JSON types
		 */
		public JSONObject jsonify() throws JSONException {
			JSONObject root = new JSONObject();
			JSONArray points = new JSONArray();
			for (Pair<Float, Float> point : pathPoints) {
				JSONArray xy = new JSONArray();
				xy.put(point.first);
				xy.put(point.second);
				points.put(xy);
			}
			root.put(JSON_PATH, points);
			root.put(JSON_X, x);
			root.put(JSON_Y, y);
			root.put(JSON_PAINT_COLOR, paint.getColor());
			return root;
		}
		
		/**
		 * Load the given {@link JSONObject} into this {@link LayerPath}. JSON should conform to the output of {@link #jsonify()}
		 *
		 * @param root the JSON to load from
		 * @throws JSONException thrown if loaded data was not valid JSON
		 */
		public void objectify(JSONObject root) throws JSONException {
			
			this.pathPoints = new ArrayList<>();
			this.path = new Path();
			
			this.paint = initPaint(root.getInt(JSON_PAINT_COLOR));
			this.y = (float) root.getDouble(JSON_Y);
			this.x = (float) root.getDouble(JSON_X);
			JSONArray points = root.getJSONArray(JSON_PATH);
			for (int i = 0; i < points.length(); i++) {
				JSONArray point = points.getJSONArray(i);
				float x = (float) point.getDouble(0);
				float y = (float) point.getDouble(1);
				if (i == 0) {
					path.moveTo(x, y);
				} else {
					path.lineTo(x, y);
				}
				this.pathPoints.add(new Pair<>(x, y));
			}
		}
		
	}
}
