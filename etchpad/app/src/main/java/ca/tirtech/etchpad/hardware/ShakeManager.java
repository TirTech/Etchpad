package ca.tirtech.etchpad.hardware;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import androidx.core.util.Consumer;

import static android.content.Context.SENSOR_SERVICE;

public class ShakeManager implements SensorEventListener {
	
	private static final String TAG = "Shake Manager";
	private static final float SHAKE_THRESHOLD = 2.5f;
	private static final float SHAKE_DELAY = 500;
	private static final long SHAKE_RESET_TIMEOUT = 1500;
	
	private final SensorManager sensorManager;
	private final Sensor accelerometer;
	private long lastShake = 0;
	private int shakeCount = 0;
	private Consumer<Integer> shakeListener;
	
	public ShakeManager(Context owner) {
		sensorManager = (SensorManager) owner.getSystemService(SENSOR_SERVICE);
		accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
	}
	
	public void setShakeListener(Consumer<Integer> shakeListener) {
		this.shakeListener = shakeListener;
	}
	
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
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
	
	public void start() {
		sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
	}
	
	public void stop() {
		sensorManager.unregisterListener(this);
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		//Do nothing
	}
}
