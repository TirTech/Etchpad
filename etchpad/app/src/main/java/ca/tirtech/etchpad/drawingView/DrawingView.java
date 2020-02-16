package ca.tirtech.etchpad.drawingView;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

public class DrawingView extends View {
	
	private static final String TAG = "Drawing View";
	private DrawingModel model;
	
	public DrawingView(Context context) {
		super(context);
		initModel(getActivity(context));
	}
	
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
		model.getLayer().observe(activity, layer -> {
			invalidate();
		});
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawColor(Color.WHITE);
		if (model.getLayer().getValue() != null) {
			model.getLayer().getValue().draw(canvas);
		}
	}
	
	public DrawingModel getModel() {
		return model;
	}
}
