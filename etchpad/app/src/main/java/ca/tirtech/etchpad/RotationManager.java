package ca.tirtech.etchpad;

import android.app.Activity;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.icu.text.DecimalFormat;
import android.util.Log;
import androidx.core.util.Consumer;
import androidx.preference.PreferenceManager;

import static android.content.Context.SENSOR_SERVICE;

public class RotationManager implements SensorEventListener {
	
	private static final DecimalFormat formatter = new DecimalFormat("#.000");
	private static final String TAG = "Rotation Manager";
	private static final float SHAKE_THRESHOLD = 2.5f;
	private static final float SHAKE_DELAY = 500;
	private static final long SHAKE_RESET_TIMEOUT = 1500;
	
	private final SensorManager sensorManager;
	private final Sensor rotationVector;
	private final Sensor accelerometer;
	private Consumer<float[]> rotationListener;
	private final SharedPreferences sharedPreferences;
	private float[] baseVector = null;
	private float deadzone;
	private long lastShake = 0;
	private int shakeCount = 0;
	private Consumer<Integer> shakeListener;
	
	public RotationManager(Activity owner) {
		sensorManager = (SensorManager) owner.getSystemService(SENSOR_SERVICE);
		rotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
		accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		sensorManager.registerListener(this, rotationVector, SensorManager.SENSOR_DELAY_GAME);
		sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(owner);
	}
	
	public void setRotationListener(Consumer<float[]> rotationListener) {
		this.rotationListener = rotationListener;
	}
	
	public void setShakeListener(Consumer<Integer> shakeListener) {
		this.shakeListener = shakeListener;
	}
	
	public void start() {
		sensorManager.registerListener(this, rotationVector, SensorManager.SENSOR_DELAY_GAME);
		sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
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
		if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
			if (baseVector == null) {
				baseVector = event.values.clone();
			}
			//String out = "[" + event.timestamp + "]" + event.values[0] + "\n" + event.values[1] + "\n" + event.values[2] + "\n-----";
			//Log.i(TAG, out);
			float[] vector = getRelativeRotation(event.values);
			if (Math.abs(vector[1]) > deadzone)
				vector[1] -= vector[1] > 0 ? deadzone : -deadzone;
			else
				vector[1] = 0;
			
			if (Math.abs(vector[2]) > deadzone)
				vector[2] -= vector[2] > 0 ? deadzone : -deadzone;
			else
				vector[2] = 0;
			
			if ((vector[1] != 0 || vector[2] != 0) && rotationListener != null) {
				rotationListener.accept(vector);
			}
		} else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			double gX = event.values[0] / SensorManager.GRAVITY_EARTH;
			double gY = event.values[1] / SensorManager.GRAVITY_EARTH;
			double gZ = event.values[2] / SensorManager.GRAVITY_EARTH;
			double gForce = Math.sqrt(gX * gX + gY * gY + gZ * gZ);
			
			if (gForce > SHAKE_THRESHOLD) {
				final long now = System.currentTimeMillis();
				Log.i(TAG, "Shake last: " + lastShake + "\nDiff: " + (now - lastShake));
				//space out shakes
				if (lastShake + SHAKE_DELAY > now) return;
				//reset shake count after some time
				if (now - lastShake > SHAKE_RESET_TIMEOUT) {
					shakeCount = 0;
				}
				lastShake = now;
				shakeCount++;
				Log.i(TAG, "Shaken: " + shakeCount);
				if (shakeListener != null) {
					shakeListener.accept(shakeCount);
				}
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
