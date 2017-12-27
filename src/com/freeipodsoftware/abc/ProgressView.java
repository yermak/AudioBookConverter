package com.freeipodsoftware.abc;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.MessageBox;

import java.text.DecimalFormat;

public class ProgressView extends ProgressViewGui {
    public ProgressView(Composite parent, int style) {
        super(parent, style);
        this.infoLabel.setText("");
        this.cancelButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                MessageBox msgbox = new MessageBox(ProgressView.this.getShell(), 196);
                msgbox.setText(Messages.getString("ProgressView.confirmation"));
                msgbox.setMessage(Messages.getString("ProgressView.cancelConfirmText"));
                int result = msgbox.open();
                if (result == 64) {
                    ProgressView.this.canceled = true;
                    ProgressView.this.cancelButton.setEnabled(false);
                    ProgressView.this.pauseButton.setSelection(false);
                    ProgressView.this.pauseButton.setEnabled(false);
                }

            }
        });
    }

    public void setButtonWidthHint(int buttonWidthHint) {
        this.setWidthHintForControl(buttonWidthHint, this.cancelButton);
        this.setWidthHintForControl(buttonWidthHint, this.pauseButton);
    }

    private void setWidthHintForControl(int buttonWidthHint, Control widget) {
        GridData gridData = (GridData) widget.getLayoutData();
        gridData.widthHint = buttonWidthHint;
    }

    public void setProgress(int progress) {
        this.progressBar.setSelection(progress);
    }

    public void setElapsedTime(long elapsedTime) {
        this.elapsedTimeLabel.setText(Messages.getString("ProgressView.timeElapsed") + " " + this.formatTime(elapsedTime));
    }

    private String formatTime(long millis) {
        DecimalFormat decimalFormat = new DecimalFormat("00");
        long sec = millis / 1000L % 60L;
        long min = millis / 60L / 1000L % 60L;
        long hrs = millis / 60L / 60L / 1000L;
        return hrs + ":" + decimalFormat.format(min) + ":" + decimalFormat.format(sec);
    }

    public void setEstimatedFinalOutputSize(long bytes) {
        if (bytes == -1L) {
            this.outputFileSizeValueLabel.setText("---");
        } else {
            DecimalFormat mbFormat = new DecimalFormat("0.0");
            this.outputFileSizeValueLabel.setText(mbFormat.format((double) bytes / 1048576.0D) + " MB");
        }

    }

    public void setRemainingTime(long remainingTime) {
        this.remainingTimeLabel.setText(Messages.getString("ProgressView.timeRemaining") + " " + this.formatTime(remainingTime));
    }

    public void setInfoText(String infoText) {
        this.infoLabel.setText(infoText);
    }

    public boolean isCanceled() {
        return this.canceled;
    }

    public boolean isPaused() {
        return this.pauseButton.getSelection();
    }

    public void reset() {
        this.canceled = false;
        this.cancelButton.setEnabled(true);
        this.pauseButton.setEnabled(true);
    }

    public void finished() {
        this.cancelButton.setEnabled(false);
        this.pauseButton.setSelection(false);
        this.pauseButton.setEnabled(false);
        this.progressBar.setSelection(0);
    }
}
