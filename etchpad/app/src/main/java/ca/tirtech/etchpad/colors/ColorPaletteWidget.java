package ca.tirtech.etchpad.colors;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import ca.tirtech.etchpad.drawingView.DrawingModel;

import java.util.ArrayList;

public class ColorPaletteWidget extends View {
	
	private static final String TAG = "Color Palette Widget";
	private final float BORDER_THICKNESS = 5f;
	private final Paint BORDER_PAINT;
	private final Paint SELECTION_PAINT;
	
	private DrawingModel model;
	private ArrayList<Paint> paints = new ArrayList<>();
	
	public ColorPaletteWidget(Context context) {
		super(context);
		BORDER_PAINT = initBorderPaint();
		SELECTION_PAINT = initSelectionPaint();
		initModel(getActivity(context));
		computePaints();
	}
	
	public ColorPaletteWidget(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		BORDER_PAINT = initBorderPaint();
		SELECTION_PAINT = initSelectionPaint();
		initModel(getActivity(context));
		computePaints();
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
		model.getColorPalette().observe(activity, pallet -> {
			computePaints();
			invalidate();
		});
	}
	
	private Paint initSelectionPaint() {
		Paint p = new Paint();
		p.setAntiAlias(false);
		p.setStyle(Paint.Style.STROKE);
		p.setStrokeWidth(BORDER_THICKNESS);
		p.setColor(Color.BLACK);
		return p;
	}
	
	private Paint initBorderPaint() {
		Paint p = new Paint();
		p.setAntiAlias(false);
		p.setStyle(Paint.Style.STROKE);
		p.setStrokeWidth(BORDER_THICKNESS);
		p.setColor(Color.BLACK);
		p.setShadowLayer(10, 0, 10, Color.BLACK);
		setLayerType(LAYER_TYPE_SOFTWARE, BORDER_PAINT);
		return p;
	}
	
	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);
		float regionSize = (getWidth() - (2 * BORDER_THICKNESS)) / (float) paints.size();
		for (int i = 0; i < paints.size(); i++) {
			canvas.drawRect((i * regionSize) + BORDER_THICKNESS, 0, ((i + 1) * regionSize) + BORDER_THICKNESS, getHeight(), paints.get(i));
			if (model.getColorPalette().getValue().getSelectedColorIndex() == i) {
				canvas.drawRect((i * regionSize) + BORDER_THICKNESS, BORDER_THICKNESS, ((i + 1) * regionSize), getHeight() - BORDER_THICKNESS, SELECTION_PAINT);
			}
		}
		canvas.drawRect(0, 0, getWidth(), getHeight(), BORDER_PAINT);
		
	}
	
	private void computePaints() {
		ArrayList<Integer> colors = model.getColorPalette().getValue().getColors();
		while (paints.size() != colors.size()) {
			if (paints.size() < colors.size()) {
				Paint newPaint = new Paint();
				newPaint.setAntiAlias(false);
				newPaint.setStyle(Paint.Style.FILL_AND_STROKE);
				newPaint.setStrokeWidth(5f);
				paints.add(newPaint);
			} else {
				paints.remove(paints.size() - 1);
			}
		}
		for (int i = 0; i < colors.size(); i++) {
			paints.get(i).setColor(colors.get(i));
		}
	}
}
