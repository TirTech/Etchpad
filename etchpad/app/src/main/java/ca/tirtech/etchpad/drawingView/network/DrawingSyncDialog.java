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

/**
 * Class used for displaying the status of remote connection establishment. This class does not contain any lifecycle but rather provides methods
 * for displaying lifecycle status and prompting user responses.
 */
public class DrawingSyncDialog {
	
	private ProgressBar syncProgress;
	private AlertDialog dialog;
	private TextView txtMessage;
	private EditText txtPrompt;
	
	/**
	 * Create a dialog for the given context with a max progress amount.
	 *
	 * @param context the context to display this dialog in
	 * @param titleId the title of the dialog
	 * @param max     the max progress to display in the progress bar
	 */
	public DrawingSyncDialog(Context context, int titleId, int max) {
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
		if (txtPrompt != null) txtPrompt.setVisibility(View.GONE);
		syncProgress.setMax(max);
		syncProgress.setProgress(0);
	}
	
	/**
	 * Set the status message and progress of the dialog.
	 *
	 * @param progress the current progress
	 * @param message  the message to display
	 */
	public void setStatus(int progress, String message) {
		txtMessage.setText(message);
		syncProgress.setProgress(progress);
		txtPrompt.setVisibility(View.GONE);
		Button btnPos = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
		Button btnNeg = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
		btnNeg.setVisibility(View.GONE);
		btnPos.setVisibility(View.GONE);
	}
	
	/**
	 * Set the status of the dialog, and allow the user to press "Cancel".
	 *
	 * @param progress the progress to set
	 * @param message  the message to display
	 * @param onAction the callback for when cancel is pressed
	 */
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
	
	/**
	 * Set the progress and message and allow the user to enter a value in a text field.
	 *
	 * @param progress the progress to set
	 * @param message  the message to display
	 * @param onAction the consumer for the entered value
	 */
	public void promptForValue(int progress, String message, Consumer<String> onAction) {
		syncProgress.setProgress(progress);
		txtMessage.setText(message);
		txtPrompt.setVisibility(View.VISIBLE);
		Button btnPos = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
		btnPos.setText("Ok");
		btnPos.setVisibility(View.VISIBLE);
		btnPos.setOnClickListener((v) -> onAction.accept(txtPrompt.getText().toString()));
	}
	
	/**
	 * Set the progress and message and allow the user to choose "Yes" or "No". The message should be phrased as a question.
	 *
	 * @param progress the progress to set
	 * @param message  the question to ask
	 * @param onAction the consumer for the user's response; true if yes
	 */
	public void promptForConfirmation(int progress, String message, Consumer<Boolean> onAction) {
		syncProgress.setProgress(progress);
		txtMessage.setText(message);
		txtPrompt.setVisibility(View.GONE);
		
		Button btnPos = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
		btnPos.setText(R.string.yes);
		btnPos.setVisibility(View.VISIBLE);
		btnPos.setOnClickListener((v) -> onAction.accept(true));
		
		Button btnNeg = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
		btnNeg.setText(R.string.no);
		btnNeg.setVisibility(View.VISIBLE);
		btnNeg.setOnClickListener((v) -> onAction.accept(false));
	}
	
	/**
	 * Dismiss the dialog.
	 */
	public void close() {
		dialog.dismiss();
	}
}
