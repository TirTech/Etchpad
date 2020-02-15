package ca.tirtech.etchpad.drawingView;


import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.EditText;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;
import ca.tirtech.etchpad.R;
import ca.tirtech.etchpad.colors.ColorPalette;
import ca.tirtech.etchpad.hardware.FileUtils;
import ca.tirtech.etchpad.hardware.InteractionService;
import ca.tirtech.etchpad.mvvm.DeepLiveData;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Date;

public class DrawingModel extends AndroidViewModel {
	
	private static final String TAG = "Drawing Model";
	private final SharedPreferences sharedPreferences;
	private DeepLiveData<DrawingLayer> layer;
	private MutableLiveData<Integer> sensitivity;
	private MutableLiveData<Boolean> lockMovement;
	private MutableLiveData<Boolean> shakeLock;
	private DeepLiveData<ColorPalette> colorPalette;
	
	public DrawingModel(Application application) {
		super(application);
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application);
		lockMovement = new MutableLiveData<>();
		shakeLock = new MutableLiveData<>();
		layer = new DeepLiveData<>();
		sensitivity = new MutableLiveData<>();
		colorPalette = new DeepLiveData<>();
		lockMovement.setValue(false);
		shakeLock.setValue(false);
		layer.setValue(new DrawingLayer(1000, 600));
		sensitivity.setValue(sharedPreferences.getInt("pen_sensitivity", 50));
		colorPalette.setValue(new ColorPalette());
		InteractionService.getInstance().setOnRotation(this::onRotation);
		InteractionService.getInstance().setOnShake(this::onShake);
		InteractionService.getInstance().addGestureDetector(new GestureDetector(application, new GestureDetector.SimpleOnGestureListener() {
			@Override
			public boolean onSingleTapConfirmed(MotionEvent e) {
				colorPalette.getValue().nextColor();
				getLayer().getValue().setColor(colorPalette.getValue().getSelectedColor());
				return true;
			}
		}));
	}
	
	/**
	 * Save this view as a JSON file. Will prompt for the file name
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
						String json = layer.getValue().jsonify().toString();
						Log.d(TAG, json);
						FileUtils.writeToFile(context, in.isEmpty() ? jsonFileName : in, ".json", Environment.DIRECTORY_DOCUMENTS, json);
						lockMovement.setValue(false);
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
	 * Load this view from a JSON file
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
						layer.setValue(new DrawingLayer(root));
						lockMovement.setValue(false);
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
	 * Export this view as a JPEG file
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
	
	public MutableLiveData<Integer> getSensitivity() {
		return sensitivity;
	}
	
	public MutableLiveData<Boolean> getLockMovement() {
		return lockMovement;
	}
	
	public MutableLiveData<Boolean> getShakeLock() {
		return shakeLock;
	}
	
	public DeepLiveData<ColorPalette> getColorPalette() {
		return colorPalette;
	}
	
	public MutableLiveData<DrawingLayer> getLayer() {
		return layer;
	}
	
	public void onRotation(float[] vals) {
		if (lockMovement.getValue()) return;
		float xOffset = vals[2] * sensitivity.getValue();
		float yOffset = -1 * vals[1] * sensitivity.getValue();
		if (layer != null) {
			layer.getValue().lineToByOffset(xOffset, yOffset);
		}
	}
	
	public void onShake(Integer shakeCount) {
		Log.i(TAG, "Shake lock is " + (shakeLock.getValue().booleanValue() ? "on" : "off"));
		if (shakeCount < 2) {
			shakeLock.setValue(false);
		}
		
		if (shakeCount >= 2 && layer.getValue() != null && !shakeLock.getValue().booleanValue()) {
			layer.getValue().undo();
			shakeLock.setValue(true);
		}
	}
}
