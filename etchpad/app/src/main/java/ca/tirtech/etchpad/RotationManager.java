package ca.tirtech.etchpad;

import android.app.Activity;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.icu.text.DecimalFormat;
import android.util.Log;
import android.widget.TextView;
import androidx.core.util.Consumer;
import androidx.preference.PreferenceManager;

import static android.content.Context.SENSOR_SERVICE;

public class RotationManager implements SensorEventListener {
	
	private static final DecimalFormat formatter = new DecimalFormat("#.000");
	private static final String TAG = "Rotation Manager";
	
	private final SensorManager sensorManager;
	private final Sensor rotationVector;
	private final TextView[] outViews;
	private final Consumer<float[]> listener;
	private final SharedPreferences sharedPreferences;
	private float[] baseVector = null;
	private float deadzone;
	
	public RotationManager(Activity owner, TextView[] views, Consumer<float[]> listener) {
		sensorManager = (SensorManager) owner.getSystemService(SENSOR_SERVICE);
		rotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
		outViews = views;
		this.listener = listener;
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(owner);
	}
	
	public void start() {
		sensorManager.registerListener(this, rotationVector, SensorManager.SENSOR_DELAY_NORMAL);
		baseVector = null;
		this.deadzone = sharedPreferences.getInt("pen_deadzone", 10) / 1000.0f;
		Log.i(TAG, "Deadzone is " + deadzone);
	}
	
	public void stop() {
		sensorManager.unregisterListener(this);
		baseVector = null;
	}
	
	@Override
	public void onSensorChanged(SensorEvent event) {
		if (baseVector == null) {
			baseVector = event.values.clone();
		}
		if (outViews == null) {
			String out = "[" + event.timestamp + "]" + event.values[0] + "\n" + event.values[1] + "\n" + event.values[2] + "\n-----";
			Log.i(TAG, out);
		} else {
			float[] vector = getRelativeRotation(event.values);
			outViews[0].setText("X: " + formatter.format(vector[2]));
			outViews[1].setText("Y: " + formatter.format(vector[1]));
			outViews[2].setText("Z: " + formatter.format(vector[0]));
			if (Math.abs(vector[1]) > deadzone || Math.abs(vector[2]) > deadzone) {
				listener.accept(vector);
			}
		}
	}
	
	private float[] getRelativeRotation(float[] vector) {
		float[] newVector = new float[3];
		float[] rotMatrix = new float[9];
		SensorManager.getRotationMatrixFromVector(rotMatrix, vector);
		SensorManager.getOrientation(rotMatrix, newVector);
		return newVector;
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// Do nothing
	}
}
