package ca.tirtech.etchpad.hardware;

import android.content.Context;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.GestureDetector;
import android.view.MotionEvent;
import androidx.core.util.Consumer;

import java.util.ArrayList;

/**
 * Service for managing sensor-based interactions. This class is a singleton.
 */
public class InteractionService {
	
	public static final long VIBRATE_SHORT = 75;
	private static InteractionService instance;
	private final RotationManager rotationManager;
	private final ShakeManager shakeManager;
	private final ArrayList<GestureDetector> gestureDetectors;
	private final Vibrator vibrationService;
	
	/**
	 * Create a new instance and initialize all managers.
	 *
	 * @param context the context for this service
	 */
	private InteractionService(Context context) {
		rotationManager = new RotationManager(context);
		shakeManager = new ShakeManager(context);
		vibrationService = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
		gestureDetectors = new ArrayList<>();
	}
	
	/**
	 * Create a new instance of this singleton, or return the existing instance.
	 *
	 * @param context the context for the service
	 */
	public static void init(Context context) {
		if (instance != null) return;
		instance = new InteractionService(context);
	}
	
	/**
	 * Get the instance of this class. This will throw a {@link NotInitializedException} if {@link #init(Context)} has not been called.
	 *
	 * @return the instance
	 */
	public static InteractionService getInstance() {
		if (instance == null) {
			throw new NotInitializedException("InteractionService was not instantiated before use. Call init() before using instance");
		}
		return instance;
	}
	
	/**
	 * Stop all sensor managers. This should be invoked when the activity is paused.
	 */
	public static void disable() {
		if (instance == null) return;
		instance.rotationManager.stop();
		instance.shakeManager.stop();
	}
	
	/**
	 * Start all sensor managers. This should be invoked when the activity is resumed.
	 */
	public static void enable() {
		if (instance == null) return;
		instance.rotationManager.start();
		instance.shakeManager.start();
	}
	
	/**
	 * Handles touch events from an activity. This should be invoked from the activity.
	 *
	 * @param e the event to handle
	 * @return whether the event was handled. Will be true if any listeners handled the event.
	 */
	public static boolean onTouchEvent(MotionEvent e) {
		if (instance == null) return false;
		boolean result = false;
		for (GestureDetector detector : getInstance().gestureDetectors) {
			result = result || detector.onTouchEvent(e);
		}
		return result;
	}
	
	/**
	 * Restarts the rotation manager to reset the baseline.
	 */
	public void centerRotation() {
		if (instance == null) return;
		rotationManager.stop();
		rotationManager.start();
	}
	
	/**
	 * Set the consumer for rotation events.
	 *
	 * @param func the consumer
	 */
	public void setOnRotation(Consumer<float[]> func) {
		rotationManager.setRotationListener(func);
	}
	
	/**
	 * Set the consumer for shake events.
	 *
	 * @param func the consumer
	 */
	public void setOnShake(Consumer<Integer> func) {
		shakeManager.setShakeListener(func);
	}
	
	/**
	 * Add a listener for gestures.
	 *
	 * @param detector the detector for touch events
	 */
	public void addGestureDetector(GestureDetector detector) {
		gestureDetectors.add(detector);
	}
	
	/**
	 * Remove the gesture detector for touch events.
	 *
	 * @param detector the detector to remove
	 */
	public void removeGestureDetector(GestureDetector detector) {
		gestureDetectors.remove(detector);
	}
	
	/**
	 * Vibrate the device according to an effect.
	 *
	 * @param ve the vibration effect
	 */
	public void vibrate(VibrationEffect ve) {
		vibrationService.vibrate(ve);
	}
}
