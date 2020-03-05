package ca.tirtech.etchpad.drawingView.network;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.core.util.Consumer;
import ca.tirtech.etchpad.R;

public class DrawingSyncDialog {
	
	private ProgressBar syncProgress;
	private AlertDialog dialog;
	private TextView txtMessage;
	private EditText txtPrompt;
	
	public DrawingSyncDialog(Context context, int titleId, int max) {
		createSyncDialog(context, titleId, max);
	}
	
	private void createSyncDialog(Context context, int titleId, int max) {
		dialog = new AlertDialog.Builder(context)
				.setView(R.layout.sync_dialog)
				.setTitle(titleId)
				.setPositiveButton("TEST", null)
				.setNegativeButton("TEST", null)
				.show();
		dialog.setCanceledOnTouchOutside(false);
		dialog.setCancelable(false);
		syncProgress = dialog.findViewById(R.id.pb_sync_progress);
		txtMessage = dialog.findViewById(R.id.txt_message);
		txtPrompt = dialog.findViewById(R.id.txt_prompt);
		txtPrompt.setVisibility(View.GONE);
		syncProgress.setMax(max);
		syncProgress.setProgress(0);
	}
	
	public void setStatus(int progress, String message) {
		txtMessage.setText(message);
		syncProgress.setProgress(progress);
		txtPrompt.setVisibility(View.GONE);
		Button btnPos = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
		Button btnNeg = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
		btnNeg.setVisibility(View.GONE);
		btnPos.setVisibility(View.GONE);
	}
	
	public void setStatusWithCancel(int progress, String message, Consumer<View> onAction) {
		txtMessage.setText(message);
		syncProgress.setProgress(progress);
		txtPrompt.setVisibility(View.GONE);
		Button btnNeg = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
		Button btnPos = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
		btnNeg.setText("Cancel");
		btnNeg.setVisibility(View.VISIBLE);
		btnPos.setVisibility(View.GONE);
		btnNeg.setOnClickListener(onAction::accept);
	}
	
	public void promptForValue(int progress, String message, Consumer<String> onAction) {
		syncProgress.setProgress(progress);
		txtMessage.setText(message);
		txtPrompt.setVisibility(View.VISIBLE);
		Button btnPos = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
		btnPos.setText("Ok");
		btnPos.setVisibility(View.VISIBLE);
		btnPos.setOnClickListener((v) -> onAction.accept(txtPrompt.getText().toString()));
	}
	
	public void promptForConfirmation(int progress, String message, Consumer<Boolean> onAction) {
		syncProgress.setProgress(progress);
		txtMessage.setText(message);
		txtPrompt.setVisibility(View.GONE);
		
		Button btnPos = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
		btnPos.setText("Yes");
		btnPos.setVisibility(View.VISIBLE);
		btnPos.setOnClickListener((v) -> onAction.accept(true));
		
		Button btnNeg = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
		btnNeg.setText("No");
		btnNeg.setVisibility(View.VISIBLE);
		btnNeg.setOnClickListener((v) -> onAction.accept(false));
	}
	
	public void close() {
		dialog.dismiss();
	}
}
