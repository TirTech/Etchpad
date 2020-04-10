package ca.tirtech.etchpad.drawingView;

import android.graphics.*;
import androidx.core.util.Pair;
import androidx.databinding.Bindable;
import androidx.databinding.library.baseAdapters.BR;
import ca.tirtech.etchpad.mvvm.LiveDataObservable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Stack;

/**
 * Stores a collection of colored paths. Paths are drawn by traversing from the starting location of the layer
 * to another point or offset. The layer can be drawn on a canvas by calling {@link #draw(Canvas)}.
 * <p/>
 * DrawingLayers may be created from JSON. The JSON may be generated using {@link #jsonify()} on an existing object and can be loaded
 * using the appropriate constructor, or may be used to replace an existing layer using {@link #objectify(JSONObject)},
 * though this is discouraged.
 */
public class DrawingLayer extends LiveDataObservable {
	public static final String JSON_LAYER_PATHS = "layer_paths";
	public static final String JSON_PAINT_COLOR = "paint_color";
	public static final String JSON_X = "x";
	public static final String JSON_Y = "y";
	public static final String JSON_PATH = "path";
	public static final String JSON_PAINT_SIZE = "paint_size";
	private float[] transformation = new float[]{0f, 0f};
	private final float[] screenOrigin = new float[]{0, 0};
	private Stack<LayerPath> paths = new Stack<>();
	private static final Paint textPaint = initPaint(Color.BLACK, 1f, Paint.Style.FILL_AND_STROKE);
	private String nickname = "";
	private float paintSize = 5f;
	
	/**
	 * Construct a blank layer. The pen on this layer will start at the provided {@code (x,y)} position.
	 *
	 * @param width  starting x pos
	 * @param height starting y pos
	 */
	public DrawingLayer(float width, float height) {
		super();
		setScreenOrigin(width, height);
		paths.push(new LayerPath(initPaint(Color.RED, paintSize), screenOrigin[0], screenOrigin[1]));
	}
	
	/**
	 * Creates a new drawing layer from the given JSON. This is a convenience constructor, replacing the following:<br/><br/>
	 * {@code
	 * DrawingLayer layer = new DrawingLayer(0,0);<br/>
	 * layer.{@link #objectify(JSONObject)};
	 * }
	 *
	 * @param root the JSON to load from
	 * @throws JSONException JSON was invalid
	 */
	public DrawingLayer(JSONObject root) throws JSONException {
		super();
		objectify(root);
	}
	
	/**
	 * Creates a new drawing layer, initialized from another layer. This is equivalent to:<br/><br/>
	 * {@code
	 * JsonObject oldJson = oldLayer.jsonify();
	 * DrawingLayer layer = new DrawingLayer(oldJson);
	 * }
	 *
	 * @param layer the layer to clone
	 * @throws JSONException JSON was invalid
	 */
	@SuppressWarnings ("CopyConstructorMissesField")
	public DrawingLayer(DrawingLayer layer) throws JSONException {
		this(layer.jsonify());
	}
	
	/**
	 * Create a new {@link Paint}.
	 *
	 * @param color  the color of the paint
	 * @param stroke the weight of the stroke
	 * @return a new {@link Paint} with the specified color
	 */
	private static Paint initPaint(int color, float stroke) {
		return initPaint(color, stroke, Paint.Style.STROKE);
	}
	
	/**
	 * Get the current transformation being applied to the layer.
	 *
	 * @return the current transformation
	 */
	@Bindable
	public float[] getTransformation() {
		return transformation;
	}
	
	/**
	 * Set the origin of the screen. Used for centring the pen.
	 *
	 * @param width  width
	 * @param height height
	 */
	public void setScreenOrigin(float width, float height) {
		screenOrigin[0] = width / 2;
		screenOrigin[1] = height / 2;
	}
	
	/**
	 * Centers the cursor on the screen.
	 */
	public void centerOnCursor() {
		transformation[0] = screenOrigin[0] - getCurrentLayerPath().x;
		transformation[1] = screenOrigin[1] - getCurrentLayerPath().y;
		notifyChange();
	}
	
	/**
	 * Set the transformation to apply to this view.
	 *
	 * @param transformation the new transformation
	 */
	public void setTransformation(float[] transformation) {
		this.transformation = transformation;
		notifyPropertyChanged(BR.transformation);
	}
	
	/**
	 * Clears the layer, reverting to the default path.
	 */
	public void clear() {
		Paint currentPaint = getCurrentLayerPath().paint;
		this.paths.clear();
		this.paths.push(new LayerPath(currentPaint, screenOrigin[0], screenOrigin[1]));
		transformation[0] = 0;
		transformation[1] = 0;
		notifyPropertyChanged(BR.currentLayerPath);
		notifyPropertyChanged(BR.transformation);
	}
	
	/**
	 * Create a new {@link Paint}.
	 *
	 * @param color  the color of the paint
	 * @param stroke the weight of the stroke
	 * @param style  the style of paint to use
	 * @return a new {@link Paint} with the specified color
	 */
	private static Paint initPaint(int color, float stroke, Paint.Style style) {
		Paint paint = new Paint();
		paint.setAntiAlias(false);
		paint.setColor(color);
		paint.setStrokeJoin(Paint.Join.BEVEL);
		paint.setStyle(style);
		paint.setStrokeWidth(stroke);
		paint.setTextSize(24);
		paint.setTextAlign(Paint.Align.CENTER);
		return paint;
	}
	
	/**
	 * Set the nickname displayed above the cursor.
	 *
	 * @param nickname the name to display
	 */
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	
	/**
	 * Get the color of the current path.
	 *
	 * @return the current path's paint's color
	 */
	@Bindable
	public int getCurrentPaintColor() {
		return getCurrentLayerPath().paint.getColor();
	}
	
	/**
	 * Draws all elements of this layer on the provided {@link Canvas}.
	 *
	 * @param canvas the canvas to draw on
	 */
	public void draw(Canvas canvas) {
		for (LayerPath lp : paths) {
			canvas.drawPath(lp.getPathTranslated(transformation), lp.paint);
		}
		LayerPath cur = getCurrentLayerPath();
		canvas.drawCircle(cur.x + transformation[0], cur.y + transformation[1], 10, cur.paint);
		canvas.drawText(nickname, cur.x + transformation[0], cur.y + transformation[1] - 30, textPaint);
		/*RectF b = calculateBounds();
		b.offset(transformation[0],transformation[1]);
		canvas.drawRect(b, cur.paint);*/
	}
	
	/**
	 * Continue the current line by (x,y) from the current position.
	 *
	 * @param x the x offset to draw to
	 * @param y the y offset to draw to
	 */
	public void lineToByOffset(float x, float y) {
		LayerPath cur = getCurrentLayerPath();
		lineTo(cur.x + x, cur.y + y);
	}
	
	/**
	 * Continue the current line to the x.
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
		notifyPropertyChanged(BR.currentLayerPath);
	}
	
	/**
	 * Get the current layer path. Defined as the layer path that is on the top of the path stack.
	 *
	 * @return the top-most layer path
	 */
	@Bindable
	public LayerPath getCurrentLayerPath() {
		return paths.peek();
	}
	
	/**
	 * Convert this DrawingLayer into a {@link JSONObject}. To recreate this object, use {@link #objectify(JSONObject)}.
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
	 * Load the given {@link JSONObject} into this DrawingLayer. JSON should conform to the output of {@link #jsonify()}.
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
		notifyPropertyChanged(BR.currentLayerPath);
	}
	
	/**
	 * Assign the color when drawing on this layer. This will create a new {@link Paint} for this color.
	 * Since each path must have its own paint, this will create a new path.
	 *
	 * @param color the color to set to
	 */
	public void setColor(int color) {
		LayerPath cur = getCurrentLayerPath();
		paths.push(new LayerPath(initPaint(color, paintSize), cur.x, cur.y));
		notifyPropertyChanged(BR.currentPaintColor);
	}
	
	/**
	 * Removes the top-most path from the layer.
	 */
	public void undo() {
		if (paths.size() > 1) {
			paths.pop();
			paintSize = getCurrentLayerPath().paint.getStrokeWidth();
			notifyPropertyChanged(BR.paintSize);
		} else if (paths.size() == 1) {
			paths.set(0, new LayerPath(initPaint(Color.RED, paintSize), screenOrigin[0], screenOrigin[1]));
			transformation[0] = 0;
			transformation[1] = 0;
			notifyPropertyChanged(BR.transformation);
		}
		notifyPropertyChanged(BR.currentLayerPath);
	}
	
	/**
	 * Get the current paint size.
	 *
	 * @return the size of the paint.
	 */
	@Bindable
	public float getPaintSize() {
		return paintSize;
	}
	
	/**
	 * Sets the size of the Paint that is drawn.
	 *
	 * @param size the size of the paint
	 */
	public void setPaintSize(float size) {
		getCurrentLayerPath().paint.setStrokeWidth(size);
		this.paintSize = size;
	}
	
	/**
	 * Calculates the rectangle that fully contains all paths in this layer.
	 *
	 * @return the computed bounds
	 */
	private RectF calculateBounds() {
		RectF allBounds = new RectF();
		RectF pathBounds = new RectF();
		for (LayerPath lp : paths) {
			lp.path.computeBounds(pathBounds, true);
			allBounds.union(pathBounds);
		}
		return allBounds;
	}
	
	/**
	 * Draws the layer on a bitmap, ensuring the full drawing is visible.
	 *
	 * @return the drawn bitmap
	 */
	public Bitmap drawForExport() {
		RectF allBounds = calculateBounds();
		Bitmap b = Bitmap.createBitmap((int) Math.ceil(allBounds.width()), (int) Math.ceil(allBounds.height()), Bitmap.Config.ARGB_8888);
		Canvas fakeCanvas = new Canvas(b);
		fakeCanvas.drawColor(Color.WHITE);
		float[] oldTransform = transformation;
		transformation = new float[]{-allBounds.left, -allBounds.top};
		draw(fakeCanvas);
		transformation = oldTransform;
		return b;
	}
	
	/**
	 * A container for settings defining a {@link Path} with a color and current position (head).
	 * Used as a substitute for {@link Pair}s of {@link Path}s and {@link Paint}s
	 */
	private static class LayerPath {
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
		 * Get the path for this LayerPath translated by the given transform.
		 *
		 * @param translation the x,y translation to apply
		 * @return the translated path
		 */
		public Path getPathTranslated(float[] translation) {
			Path newPath = new Path();
			path.offset(translation[0], translation[1], newPath);
			return newPath;
		}
		
		/**
		 * Creates a new layer from the given JSON. This is a convenience constructor, replacing the following:<br/><br/>
		 * {@code
		 * LayerPath layer = new LayerPath(Color.RED,0,0);<br/>
		 * layer.{@link #objectify(JSONObject)};
		 * }
		 *
		 * @param root the JSON to load from
		 */
		public LayerPath(JSONObject root) throws JSONException {
			objectify(root);
		}
		
		/**
		 * Convert this LayerPath into a {@link JSONObject}. To recreate this object, use {@link #objectify(JSONObject)}.
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
			root.put(JSON_PAINT_SIZE, paint.getStrokeWidth());
			return root;
		}
		
		/**
		 * Load the given {@link JSONObject} into this LayerPath. JSON should conform to the output of {@link #jsonify()}.
		 *
		 * @param root the JSON to load from
		 * @throws JSONException thrown if loaded data was not valid JSON
		 */
		public void objectify(JSONObject root) throws JSONException {
			
			this.pathPoints = new ArrayList<>();
			this.path = new Path();
			
			this.paint = initPaint(root.getInt(JSON_PAINT_COLOR), (float) root.getDouble(JSON_PAINT_SIZE));
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
