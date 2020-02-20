package ca.tirtech.etchpad.drawingView.network;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.core.util.Consumer;
import ca.tirtech.etchpad.R;

public class DrawingSyncDialog {
	
	private ProgressBar syncProgress;
	private AlertDialog dialog;
	private TextView txtMessage;
	
	public DrawingSyncDialog(Context context) {
		createSyncDialog(context);
	}
	
	private void createSyncDialog(Context context) {
		dialog = new AlertDialog.Builder(context)
				.setView(R.layout.sync_dialog)
				.setTitle(R.string.action_host)
				.show();
		syncProgress = dialog.findViewById(R.id.pb_sync_progress);
		txtMessage = dialog.findViewById(R.id.txt_message);
		syncProgress.setMax(4);
		syncProgress.setProgress(0);
	}
	
	public void setStatus(int progress, String message) {
		txtMessage.setText(message);
		syncProgress.setProgress(progress);
	}
	
	public void promptForConfirmation(int progress, String message, Consumer<Boolean> onAction) {
		syncProgress.setProgress(progress);
		txtMessage.setText(message);
		
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
