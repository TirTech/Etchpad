package ca.tirtech.etchpad;

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
import android.view.View;
import android.widget.EditText;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceManager;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Date;

public class DrawingView extends View {
	
	private static final String TAG = "Drawing View";
	private final SharedPreferences sharedPreferences;
	private DrawingLayer layer;
	private float sensitivity;
	private boolean lockMovement = false;
	
	public DrawingView(Context context) {
		super(context);
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		layer = new DrawingLayer(1000, 600);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawColor(Color.WHITE);
		if (layer != null) {
			layer.draw(canvas);
		}
	}
	
	public void onRotation(float[] vals) {
		if (lockMovement) return;
		float xOffset = vals[2] * sensitivity;
		float yOffset = -1 * vals[1] * sensitivity;
		if (layer != null) {
			layer.lineToByOffset(xOffset, yOffset);
		}
		invalidate();
	}
	
	/**
	 * Save this view as a JSON file. Will prompt for the file name
	 */
	public void save() {
		if (layer == null) return;
		lockMovement = true;
		//Prompt for file name
		String jsonFileName = "JSON_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + "_";
		final EditText input = new EditText(getContext());
		input.setHint("New File Name");
		input.setInputType(InputType.TYPE_CLASS_TEXT);
		new AlertDialog.Builder(getContext())
				.setTitle(R.string.action_save)
				.setView(input)
				.setPositiveButton("OK", (dialog, which) -> {
					try {
						String in = input.getText().toString();
						String json = layer.jsonify().toString();
						Log.d(TAG, json);
						FileUtils.writeToFile(getContext(), in.isEmpty() ? jsonFileName : in, ".json", Environment.DIRECTORY_DOCUMENTS, json);
						lockMovement = false;
					} catch (IOException | JSONException e) {
						dialog.dismiss();
						new AlertDialog.Builder(getContext())
								.setTitle(R.string.action_load)
								.setMessage("The file does not exist, or something went wrong. Please try again.")
								.setPositiveButton("OK", (d, w) -> lockMovement = false)
								.show();
					}
				})
				.setNegativeButton("Cancel", (dialog, which) -> {
					dialog.cancel();
					lockMovement = false;
				})
				.show();
	}
	
	/**
	 * Load this view from a JSON file
	 */
	public void load() {
		lockMovement = true;
		//Prompt for file name
		final EditText input = new EditText(getContext());
		input.setHint("File Name");
		input.setInputType(InputType.TYPE_CLASS_TEXT);
		new AlertDialog.Builder(getContext())
				.setTitle(R.string.action_load)
				.setView(input)
				.setPositiveButton("OK", (dialog, which) -> {
					try {
						String json = FileUtils.readFromFile(getContext(), input.getText().toString(), ".json", "", Environment.DIRECTORY_DOCUMENTS);
						Log.i(TAG, "JSON in: " + json);
						JSONObject root = new JSONObject(json);
						layer = new DrawingLayer(root);
						lockMovement = false;
					} catch (IOException | JSONException e) {
						dialog.dismiss();
						new AlertDialog.Builder(getContext())
								.setTitle(R.string.action_load)
								.setMessage("The file does not exist, or something went wrong. Please try again.")
								.setPositiveButton("OK", (d, w) -> lockMovement = false)
								.show();
					}
				})
				.setNegativeButton("Cancel", (dialog, which) -> {
					dialog.cancel();
					lockMovement = false;
				})
				.show();
	}
	
	/**
	 * Export this view as a JPEG file
	 */
	public void export() {
		if (layer == null) return;
		lockMovement = true;
		//Prompt for file name
		String jsonFileName = "JPEG_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + "_";
		final EditText input = new EditText(getContext());
		input.setHint("Image Name");
		input.setInputType(InputType.TYPE_CLASS_TEXT);
		new AlertDialog.Builder(getContext())
				.setTitle(R.string.action_export)
				.setView(input)
				.setPositiveButton("OK", (dialog, which) -> {
					try {
						String in = input.getText().toString();
						
						//Load Bitmap
						Bitmap b = Bitmap.createBitmap(this.getWidth(), this.getHeight(), Bitmap.Config.ARGB_8888);
						Canvas fakeCanvas = new Canvas(b);
						this.draw(fakeCanvas);
						ByteArrayOutputStream fos = new ByteArrayOutputStream();
						b.compress(Bitmap.CompressFormat.JPEG, 95, fos);
						fos.flush();
						byte[] bytes = fos.toByteArray();
						fos.close();
						
						//Write to file
						Path image = FileUtils.writeToFile(getContext(), in.isEmpty() ? jsonFileName : in, ".jpeg", Environment.DIRECTORY_PICTURES, bytes);
						MediaStore.Images.Media.insertImage(getContext().getContentResolver(),
								image.toString(),
								image.getFileName().toString(),
								image.getFileName().toString());
						lockMovement = false;
					} catch (IOException e) {
						dialog.dismiss();
						new AlertDialog.Builder(getContext())
								.setTitle(R.string.action_load)
								.setMessage("The file does not exist, or something went wrong. Please try again.")
								.setPositiveButton("OK", (d, w) -> lockMovement = false)
								.show();
					}
				})
				.setNegativeButton("Cancel", (dialog, which) -> {
					dialog.cancel();
					lockMovement = false;
				})
				.show();
	}
	
	public void resume() {
		this.sensitivity = sharedPreferences.getInt("pen_sensitivity", 10);
	}
	
	public void pause() {
		//called when onPause is
	}
	
	public int getPaintColor() {
		return layer != null ? layer.getColor() : 0;
	}
	
	public void setPaintColor(int color) {
		if (layer != null) {
			layer.setColor(color);
		}
	}
	
	public void clear() {
		layer = new DrawingLayer(1000, 600);
	}
}
