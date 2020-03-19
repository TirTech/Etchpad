package ca.tirtech.etchpad.drawingView;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import ca.tirtech.etchpad.mvvm.Event;
import com.google.android.material.snackbar.Snackbar;

/**
 * The view to draw the user's drawing on. Driven by a {@link DrawingModel}
 */
public class DrawingView extends View {
	
	private static final String TAG = "Drawing View";
	private DrawingModel model;
	private final Observer<Event<Integer>> snackObserver = event -> {
		if (isAttachedToWindow() && !event.isConsumed()) {
			Snackbar.make(this, event.consume(), Snackbar.LENGTH_SHORT).show();
		}
	};
	
	/**
	 * Create a view.
	 *
	 * @param context the context to use
	 */
	public DrawingView(Context context) {
		super(context);
		initModel(getActivity(context));
	}
	
	/**
	 * Create a view.
	 *
	 * @param context the context to use
	 * @param attrs   the view attributes to use
	 */
	public DrawingView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initModel(getActivity(context));
	}
	
	private AppCompatActivity getActivity(Context context) {
		while (context instanceof ContextWrapper) {
			if (context instanceof AppCompatActivity) {
				return (AppCompatActivity) context;
			}
			context = ((ContextWrapper) context).getBaseContext();
		}
		return null;
	}
	
	private void initModel(AppCompatActivity activity) {
		model = new ViewModelProvider(activity, ViewModelProvider.AndroidViewModelFactory.getInstance(activity.getApplication())).get(DrawingModel.class);
		model.setOrientation(getResources().getConfiguration().orientation);
		model.getLayer().observe(activity, layer -> invalidate());
		model.getSnackbarMessage().observe(activity, snackObserver);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawColor(Color.WHITE);
		model.getLayer().getValue();
		model.getLayer().getValue().draw(canvas);
	}
	
	/**
	 * Get the model that drives this view.
	 *
	 * @return the model
	 */
	public DrawingModel getModel() {
		return model;
	}
}
