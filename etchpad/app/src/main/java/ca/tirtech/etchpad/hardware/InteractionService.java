package ca.tirtech.etchpad.hardware;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import androidx.core.util.Consumer;

import java.util.ArrayList;

public class InteractionService {
	
	private static InteractionService instance;
	private RotationManager rotationManager;
	private ShakeManager shakeManager;
	private ArrayList<GestureDetector> gestureDetectors;
	
	private InteractionService(Context context) {
		rotationManager = new RotationManager(context);
		shakeManager = new ShakeManager(context);
		gestureDetectors = new ArrayList<>();
	}
	
	public static void init(Context context) {
		if (instance != null) return;
		instance = new InteractionService(context);
	}
	
	public static InteractionService getInstance() {
		if (instance == null) {
			throw new NotInitializedException("InteractionService was not instantiated before use. Call init() before using instance");
		}
		return instance;
	}
	
	public static void disable() {
		if (instance == null) return;
		instance.rotationManager.stop();
		instance.shakeManager.stop();
	}
	
	public static void enable() {
		if (instance == null) return;
		instance.rotationManager.start();
		instance.shakeManager.start();
	}
	
	public static boolean onTouchEvent(MotionEvent e) {
		if (instance == null) return false;
		boolean result = false;
		for (GestureDetector detector : getInstance().gestureDetectors) {
			result = result || detector.onTouchEvent(e);
		}
		return result;
	}
	
	public void centerRotation() {
		if (instance == null) return;
		rotationManager.stop();
		rotationManager.start();
	}
	
	public void setOnRotation(Consumer<float[]> func) {
		rotationManager.setRotationListener(func);
	}
	
	public void setOnShake(Consumer<Integer> func) {
		shakeManager.setShakeListener(func);
	}
	
	public void addGestureDetector(GestureDetector detector) {
		gestureDetectors.add(detector);
	}
	
	public void removeGestureDetector(GestureDetector detector) {
		gestureDetectors.remove(detector);
	}
}
