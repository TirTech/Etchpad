package ca.tirtech.etchpad.hardware;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import androidx.core.util.Consumer;
import androidx.preference.PreferenceManager;

import static android.content.Context.SENSOR_SERVICE;

/**
 * Manages rotation vector events to notify about rotation changes.
 */
public class RotationManager implements SensorEventListener {
	
	private static final String TAG = "Rotation Manager";
	
	private final SensorManager sensorManager;
	private final Sensor rotationVector;
	private Consumer<float[]> rotationListener;
	private final SharedPreferences sharedPreferences;
	private float[] baseRotMatrix = null;
	private float deadzone;
	
	/**
	 * Construct a new manager for the given context.
	 *
	 * @param owner the context for this manager
	 */
	public RotationManager(Context owner) {
		sensorManager = (SensorManager) owner.getSystemService(SENSOR_SERVICE);
		rotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
		sensorManager.registerListener(this, rotationVector, SensorManager.SENSOR_DELAY_GAME);
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(owner);
	}
	
	/**
	 * Set the listener to invoke when a rotation is detected. Will be given the pitch, roll, and azimuth of the rotation.
	 *
	 * @param rotationListener the listener for rotation events
	 */
	public void setRotationListener(Consumer<float[]> rotationListener) {
		this.rotationListener = rotationListener;
	}
	
	/**
	 * Start listening for rotation events. This will register sensor listeners.
	 */
	public void start() {
		sensorManager.registerListener(this, rotationVector, SensorManager.SENSOR_DELAY_GAME);
		baseRotMatrix = null;
		this.deadzone = sharedPreferences.getInt("pen_deadzone", 10) / 1000.0f;
	}
	
	/**
	 * Stop listening for rotation events. This will deregister sensor listeners.
	 */
	public void stop() {
		sensorManager.unregisterListener(this);
		baseRotMatrix = null;
	}
	
	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
			if (baseRotMatrix == null) {
				baseRotMatrix = new float[9];
				SensorManager.getRotationMatrixFromVector(baseRotMatrix, event.values);
			}
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
		}
	}
	
	/**
	 * Convert the raw quaternion into relatively centred euler angles for pitch, roll, and azimuth.
	 *
	 * @param vector raw rotation angles
	 * @return angles from the baseline
	 */
	private float[] getRelativeRotation(float[] vector) {
		float[] newVector = new float[3];
		float[] rotMatrix = new float[9];
		SensorManager.getRotationMatrixFromVector(rotMatrix, vector);
		SensorManager.getAngleChange(newVector, rotMatrix, baseRotMatrix);
		return newVector;
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// Do nothing
	}
}
