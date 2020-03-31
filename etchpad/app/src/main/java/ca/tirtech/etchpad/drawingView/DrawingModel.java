package ca.tirtech.etchpad.drawingView;


import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.os.Environment;
import android.os.VibrationEffect;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.EditText;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.AndroidViewModel;
import androidx.preference.PreferenceManager;
import ca.tirtech.etchpad.R;
import ca.tirtech.etchpad.colors.ColorPalette;
import ca.tirtech.etchpad.hardware.FileUtils;
import ca.tirtech.etchpad.hardware.InteractionService;
import ca.tirtech.etchpad.mvvm.DeepLiveData;
import ca.tirtech.etchpad.mvvm.Event;
import ca.tirtech.etchpad.mvvm.NonNullLiveData;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;

/**
 * LiveModel for storing UI information across the UI lifecycle. This class contains data about the drawing created in the app.
 * This model responds to user interactions and updates drawing information appropriately.
 * This model may also have the drawing data saved and loaded from the devices file system using the
 * {@link #save(Context)} and {@link #load(Context)} methods.
 */
public class DrawingModel extends AndroidViewModel {
	
	private static final String JSON_MODEL = "model";
	private static final String JSON_COLORS = "colors";
	private static final String TAG = "Drawing Model";
	private final SharedPreferences sharedPreferences;
	private final DeepLiveData<DrawingLayer> layer;
	private final NonNullLiveData<Integer> sensitivityPitch;
	private final NonNullLiveData<Integer> sensitivityRoll;
	private final NonNullLiveData<Boolean> lockMovement;
	private final NonNullLiveData<Boolean> shakeLock;
	private final DeepLiveData<ColorPalette> colorPalette;
	private final NonNullLiveData<Event<Integer>> snackbarMessage;
	private int orientation = Configuration.ORIENTATION_PORTRAIT;
	private SharedPreferences.OnSharedPreferenceChangeListener prefListener = (sharedPreferences, key) -> loadPreferences();
	
	/**
	 * Construct a new model given the current application.
	 *
	 * @param application the current application
	 */
	public DrawingModel(Application application) {
		super(application);
		
		float[] screenSize = new float[]{
				Resources.getSystem().getDisplayMetrics().widthPixels,
				Resources.getSystem().getDisplayMetrics().heightPixels
		};
		
		// LiveData setup
		lockMovement = new NonNullLiveData<>(false);
		shakeLock = new NonNullLiveData<>(false);
		layer = new DeepLiveData<>(new DrawingLayer(screenSize[0], screenSize[1]));
		sensitivityPitch = new NonNullLiveData<>(0);
		sensitivityRoll = new NonNullLiveData<>(0);
		colorPalette = new DeepLiveData<>(new ColorPalette());
		Event<Integer> e = new Event<>(0);
		e.consume();
		snackbarMessage = new NonNullLiveData<>(e);
		
		// Prefs
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application);
		sharedPreferences.registerOnSharedPreferenceChangeListener(prefListener);
		loadPreferences();
		
		// Interactions
		InteractionService.getInstance().setOnRotation(this::onRotation);
		InteractionService.getInstance().setOnShake(this::onShake);
		InteractionService.getInstance().addGestureDetector(new GestureDetector(getApplication(), new GestureDetector.SimpleOnGestureListener() {
			@Override
			public boolean onSingleTapConfirmed(MotionEvent e) {
				colorPalette.getValue().nextColor();
				getLayer().getValue().setColor(colorPalette.getValue().getSelectedColor());
				InteractionService.getInstance().vibrate(VibrationEffect.createOneShot(InteractionService.VIBRATE_SHORT, VibrationEffect.DEFAULT_AMPLITUDE));
				return true;
			}
			
			@Override
			public boolean onDoubleTap(MotionEvent e) {
				InteractionService.getInstance().centerRotation();
				InteractionService.getInstance().vibrate(VibrationEffect.createWaveform(new long[]{
						InteractionService.VIBRATE_SHORT,
						InteractionService.VIBRATE_SHORT,
						InteractionService.VIBRATE_SHORT,
						InteractionService.VIBRATE_SHORT}, -1));
				return true;
			}
			
			@Override
			public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
				DrawingLayer l = layer.getValue();
				float[] oldT = l.getTransformation();
				float[] dist = {oldT[0] - distanceX, oldT[1] - distanceY};
				l.setTransformation(dist);
				return true;
			}
			
			@Override
			public boolean onDown(MotionEvent e) {
				return true;
			}
		}));
	}
	
	/**
	 * Get the LiveData preventing rotations from being registered.
	 *
	 * @return LiveData for preventing rotation
	 */
	public NonNullLiveData<Boolean> getLockMovement() {
		return lockMovement;
	}
	
	/**
	 * Set the devices orientation. Assists with determining device tilts.
	 *
	 * @param orientation the device orientation.
	 */
	public void setOrientation(int orientation) {
		this.orientation = orientation;
	}
	
	/**
	 * Get the snackbarMessage LiveData. This stores a snackbar message to display.
	 *
	 * @return the LiveData
	 */
	public NonNullLiveData<Event<Integer>> getSnackbarMessage() {
		return snackbarMessage;
	}
	
	/**
	 * Create a snackbar message event to propagate to the UI.
	 *
	 * @param resId the message to send
	 */
	public void sendSnackbarMessage(@StringRes int resId) {
		snackbarMessage.setValue(new Event<>(resId));
	}
	
	/**
	 * Load the preferences from SharedPreferences.
	 */
	private void loadPreferences() {
		sensitivityPitch.setValue(sharedPreferences.getInt("pen_sensitivity_pitch", 50));
		sensitivityRoll.setValue(sharedPreferences.getInt("pen_sensitivity_roll", 50));
	}
	
	/**
	 * Save this view as a JSON file. Will prompt for the file name
	 *
	 * @param context the context to save using
	 */
	public void save(Context context) {
		if (layer == null) return;
		lockMovement.setValue(true);
		//Prompt for file name
		String jsonFileName = "JSON_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + "_";
		final EditText input = new EditText(context);
		input.setHint("New File Name");
		input.setInputType(InputType.TYPE_CLASS_TEXT);
		new AlertDialog.Builder(context)
				.setTitle(R.string.action_save)
				.setView(input)
				.setPositiveButton("OK", (dialog, which) -> {
					try {
						String in = input.getText().toString();
						JSONObject json = new JSONObject();
						JSONObject model = layer.getValue().jsonify();
						JSONArray colors = new JSONArray();
						for (int color : colorPalette.getValue().getColors()) {
							colors.put(color);
						}
						json.put(JSON_MODEL, model);
						json.put(JSON_COLORS, colors);
						FileUtils.writeToFile(context, in.isEmpty() ? jsonFileName : in, ".json", Environment.DIRECTORY_DOCUMENTS, json.toString());
						lockMovement.setValue(false);
						sendSnackbarMessage(R.string.model_save);
					} catch (IOException | JSONException e) {
						dialog.dismiss();
						new AlertDialog.Builder(context)
								.setTitle(R.string.action_load)
								.setMessage("The file does not exist, or something went wrong. Please try again.")
								.setPositiveButton("OK", (d, w) -> lockMovement.setValue(false))
								.show();
					}
				})
				.setNegativeButton("Cancel", (dialog, which) -> {
					dialog.cancel();
					lockMovement.setValue(false);
				})
				.show();
	}
	
	/**
	 * Load this view from a JSON file.
	 * @param context the context to save using
	 */
	public void load(Context context) {
		lockMovement.setValue(true);
		//Prompt for file name
		final EditText input = new EditText(context);
		input.setHint("File Name");
		input.setInputType(InputType.TYPE_CLASS_TEXT);
		new AlertDialog.Builder(context)
				.setTitle(R.string.action_load)
				.setView(input)
				.setPositiveButton("OK", (dialog, which) -> {
					try {
						String json = FileUtils.readFromFile(context, input.getText().toString(), ".json", "", Environment.DIRECTORY_DOCUMENTS);
						Log.i(TAG, "JSON in: " + json);
						JSONObject root = new JSONObject(json);
						JSONArray colors = root.getJSONArray(JSON_COLORS);
						JSONObject model = root.getJSONObject(JSON_MODEL);
						ArrayList<Integer> newColors = new ArrayList<>();
						for (int i = 0; i < colors.length(); i++) {
							newColors.add(colors.getInt(i));
						}
						colorPalette.getValue().setColors(newColors);
						layer.setValue(new DrawingLayer(model));
						lockMovement.setValue(false);
						sendSnackbarMessage(R.string.model_load);
					} catch (IOException | JSONException e) {
						dialog.dismiss();
						new AlertDialog.Builder(context)
								.setTitle(R.string.action_load)
								.setMessage("The file does not exist, or something went wrong. Please try again.")
								.setPositiveButton("OK", (d, w) -> lockMovement.setValue(false))
								.show();
					}
				})
				.setNegativeButton("Cancel", (dialog, which) -> {
					dialog.cancel();
					lockMovement.setValue(false);
				})
				.show();
	}
	
	/**
	 * Export this view as a JPEG file.
	 * @param context the context to save using
	 * @param width the width of the image to save
	 * @param height the height of the image to save
	 */
	public void export(Context context, int width, int height) {
		if (layer == null) return;
		lockMovement.setValue(true);
		//Prompt for file name
		String jsonFileName = "JPEG_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + "_";
		final EditText input = new EditText(context);
		input.setHint("Image Name");
		input.setInputType(InputType.TYPE_CLASS_TEXT);
		new AlertDialog.Builder(context)
				.setTitle(R.string.action_export)
				.setView(input)
				.setPositiveButton("OK", (dialog, which) -> {
					try {
						String in = input.getText().toString();
						//Load Bitmap
						Bitmap b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
						Canvas fakeCanvas = new Canvas(b);
						fakeCanvas.drawColor(Color.WHITE);
						layer.getValue().draw(fakeCanvas);
						ByteArrayOutputStream fos = new ByteArrayOutputStream();
						b.compress(Bitmap.CompressFormat.JPEG, 95, fos);
						fos.flush();
						byte[] bytes = fos.toByteArray();
						fos.close();
						
						//Write to file
						Path image = FileUtils.writeToFile(context, in.isEmpty() ? jsonFileName : in, ".jpeg", Environment.DIRECTORY_PICTURES, bytes);
						MediaStore.Images.Media.insertImage(context.getContentResolver(),
								image.toString(),
								image.getFileName().toString(),
								image.getFileName().toString());
						lockMovement.setValue(false);
						sendSnackbarMessage(R.string.model_export);
					} catch (IOException e) {
						dialog.dismiss();
						new AlertDialog.Builder(context)
								.setTitle(R.string.action_load)
								.setMessage("The file does not exist, or something went wrong. Please try again.")
								.setPositiveButton("OK", (d, w) -> lockMovement.setValue(false))
								.show();
					}
				})
				.setNegativeButton("Cancel", (dialog, which) -> {
					dialog.cancel();
					lockMovement.setValue(false);
				})
				.show();
	}
	
	/**
	 * Get the LiveData for the color palette.
	 *
	 * @return the color palette LiveData
	 */
	public DeepLiveData<ColorPalette> getColorPalette() {
		return colorPalette;
	}

	/**
	 * Get the LiveData for the drawing layer.
	 *
	 * @return the current drawing layer
	 */
	public NonNullLiveData<DrawingLayer> getLayer() {
		return layer;
	}
	
	/**
	 * Calculates the action to take when the device is rotated.
	 *
	 * @param vals {@code [z,y,x]} Euler angles of rotation from the start position
	 */
	public void onRotation(float[] vals) {
		if (lockMovement.getValue() || layer == null) return;
		float xOffset = vals[2] * sensitivityPitch.getValue();
		float yOffset = -1 * vals[1] * sensitivityRoll.getValue();
		if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
			float temp = xOffset;
			xOffset = -yOffset;
			yOffset = temp;
		}
		layer.getValue().lineToByOffset(xOffset, yOffset);
	}
	
	/**
	 * Determines whether to undo an action based on the number of shakes and prior shakes.
	 *
	 * @param shakeCount number of shakes
	 */
	public void onShake(Integer shakeCount) {
		Log.i(TAG, "Shake lock is " + (shakeLock.getValue() ? "on" : "off"));
		if (shakeCount < 2) {
			shakeLock.setValue(false);
		}
		
		if (shakeCount >= 2 && !shakeLock.getValue()) {
			layer.getValue().undo();
			shakeLock.setValue(true);
			InteractionService.getInstance().vibrate(VibrationEffect.createOneShot(125, VibrationEffect.DEFAULT_AMPLITUDE));
		}
	}
	
	public void setPaintSize(float size) {
		layer.getValue().setPaintSize(size);
	}
}
