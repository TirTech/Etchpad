package ca.tirtech.etchpad;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.icu.text.SimpleDateFormat;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import androidx.preference.PreferenceManager;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.util.Date;

public class DrawingView extends View {

    private static String TAG = "Drawing View";

    private String lastFile = null;
    private DrawingLayer layer;
    private SharedPreferences sharedPreferences;
    private float sensitivity;

    public DrawingView(Activity activity) {
        super(activity);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        layer = new DrawingLayer(1000, 600);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (layer != null) {
            layer.draw(canvas);
        }
    }

    public void onRotation(float[] vals) {
        float xOffset = vals[2] * sensitivity;
        float yOffset = -1 * vals[1] * sensitivity;
	    if (layer != null) {
		    layer.lineToByOffset(xOffset,yOffset);
	    }
        invalidate();
    }
	
	/**
	 * Save this view as a JSON file
	 */
	public void save() {
		if (layer == null) return;
		try {
			String json;
				json = layer.jsonify().toString();
				Log.i(TAG,json);
			String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
			File storageDir = getContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
			String jsonFileName = "JSON_" + timeStamp + "_";
			final File jsonFile = File.createTempFile(
					jsonFileName,  /* prefix */
					".json",         /* suffix */
					storageDir      /* directory */
			);
			lastFile = jsonFile.getAbsolutePath();
			FileWriter out = new FileWriter(jsonFile);
			out.write(json);
			out.flush();
			out.close();
		} catch (JSONException | IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Load this view from a JSON file
	 */
	public void load() {
		try {
			if (lastFile != null) {
				String json = String.join("",Files.readAllLines(new File(lastFile).toPath()));
				Log.i(TAG,"JSON in: " + json);
				JSONObject root = new JSONObject(json);
				layer = new DrawingLayer(root);
			}
		} catch (JSONException | IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Export this view as a JPEG file
	 */
	public void export() {
        try {
            // Create an image file name
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            final File image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );
            
            Bitmap b = Bitmap.createBitmap(this.getWidth(), this.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas fakeCanvas = new Canvas(b);
            this.draw(fakeCanvas);
            
            new Thread(() -> {
                try {
                    FileOutputStream fos = new FileOutputStream(image);
                    b.compress(Bitmap.CompressFormat.JPEG, 95, fos);
                    fos.flush();
                    fos.close();

                    MediaStore.Images.Media.insertImage(getContext().getContentResolver(),image.getAbsolutePath(),image.getName(),image.getName());
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                }
            }).start();
        } catch (IOException ex) {
            Log.e(TAG,ex.getMessage());
        }
    }

    public void resume() {
        this.sensitivity = sharedPreferences.getInt("pen_sensitivity", 10);
    }

    public void pause() {
        //called when onPause is
    }

    public void setPaintColor(int color) {
	    if (layer != null) {
		    layer.setColor(color);
	    }
    }

    public int getPaintColor() {
        return layer != null ? layer.getColor() : 0;
    }
	
	public void clear() {
		layer = null;
	}
}
