package ca.tirtech.etchpad.hardware;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import androidx.core.util.Consumer;

import static android.content.Context.SENSOR_SERVICE;

/**
 * Manages device rotation and accelerations in order to provide shake-based interactions.
 */
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
	
	/**
	 * Construct a new ShakeManager for the given context.
	 *
	 * @param owner the context for the manager
	 */
	public ShakeManager(Context owner) {
		sensorManager = (SensorManager) owner.getSystemService(SENSOR_SERVICE);
		accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
	}
	
	/**
	 * Set the listener to invoke when a shake is detected. It will be passed the number of concurrent shakes.
	 *
	 * @param shakeListener the listener to set
	 */
	public void setShakeListener(Consumer<Integer> shakeListener) {
		this.shakeListener = shakeListener;
	}
	
	/**
	 * Handle acceleration events to detect shakes.
	 *
	 * @param event the sensor event
	 */
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
	
	/**
	 * Start observing shakes. This will enable sensor listeners.
	 */
	public void start() {
		sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
	}
	
	/**
	 * Stop observing shakes. This will unregister sensor listeners.
	 */
	public void stop() {
		sensorManager.unregisterListener(this);
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		//Do nothing
	}
}
