package com.freeipodsoftware.abc;

import com.freeipodsoftware.abc.conversionstrategy.BatchConversionStrategy;
import com.freeipodsoftware.abc.conversionstrategy.ConversionStrategy;
import com.freeipodsoftware.abc.conversionstrategy.JoiningConversionStrategy;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import uk.yermak.audiobookconverter.ConversionMode;
import uk.yermak.audiobookconverter.ParallelConversionStrategy;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.Date;

public class MainWindow extends MainWindowGui implements FinishListener {
    private EventDispatcher eventDispatcher = new EventDispatcher();
    private TagSuggestionStrategy tagSuggestionStrategy;
    private ProgressView progressView;
    private ConversionStrategy conversionStrategy;

    public static void main(String[] args) {
        Display display = Display.getDefault();
        MainWindow thisClass = new MainWindow();
        thisClass.create();
        thisClass.sShell.open();
        if (AppProperties.getBooleanProperty("stayUpdated") && !isUpdateCheckSuspended()) {
            checkForUpdates(thisClass.sShell);
        }

        while (!thisClass.sShell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }

        display.dispose();
    }

    public MainWindow() {
    }

    protected void create() {
        this.createSShell();
        this.startButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                MainWindow.this.startConversion();
            }
        });
        this.inputFileSelection.setEventDispatcher(this.eventDispatcher);
        this.tagSuggestionStrategy = new TagSuggestionStrategy();
        this.tagSuggestionStrategy.setEventDispatcher(this.eventDispatcher);
        this.tagSuggestionStrategy.setTagEditor(this.toggleableTagEditor.getTagEditor());
        this.tagSuggestionStrategy.setInputFileSelection(this.inputFileSelection);
        this.updateToggleableTagEditorEnablement();
        this.aboutLink2.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                MainWindow.this.showAboutDialog();
            }
        });
        this.updateLink.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                Program.launch("https://github.com/yermak/AudioBookConverter");
            }
        });
        this.websiteLink.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                Program.launch("https://github.com/yermak/AudioBookConverter");
            }
        });
        this.helpLink.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                Program.launch("https://github.com/yermak/AudioBookConverter");
            }
        });
        this.optionPanel.addOptionChangedListener(MainWindow.this::updateToggleableTagEditorEnablement);
    }

    private void updateToggleableTagEditorEnablement() {
        this.toggleableTagEditor.setEnabled(this.optionPanel.getMode() == ConversionMode.SINGLE);
    }

    private void showAboutDialog() {
        (new AboutDialog(this.sShell)).open();
    }

    private static boolean isUpdateCheckSuspended() {
        try {
            Date noUpdateCheckUntil = AppProperties.getDateProperty("noUpdateCheckUntil");
            return (new Date()).before(noUpdateCheckUntil);
        } catch (Exception var1) {
            return false;
        }
    }

    private static void checkForUpdates(Shell shell) {
        MainWindow.UpdateThread updateThread = new MainWindow.UpdateThread(shell);
        updateThread.start();
    }

    private ConversionStrategy getConversionStrategy() {
        if (this.conversionStrategy != null) {
            return this.conversionStrategy;
        }
        switch (optionPanel.getMode()) {
            case SINGLE:
                this.conversionStrategy = new JoiningConversionStrategy();
                break;
            case BATCH:
                this.conversionStrategy = new BatchConversionStrategy();
                break;
            case PARALLEL:
                this.conversionStrategy = new ParallelConversionStrategy();
                break;
        }
        return this.conversionStrategy;
    }

    private void startConversion() {
        if (this.inputFileSelection.getFileList().length != 0) {
            this.getConversionStrategy().setInputFileList(this.inputFileSelection.getFileList());
            if (this.getConversionStrategy().makeUserInterview(this.sShell)) {
                this.createProgressView();
                this.setUIEnabled(false);
                this.getConversionStrategy().setFinishListener(this);
                this.getConversionStrategy().setMp4Tags(this.toggleableTagEditor.getTagEditor().getMp4Tags());
                this.getConversionStrategy().start(this.sShell);
                ProgressUpdateThread progressUpdateThread = new ProgressUpdateThread(this.getConversionStrategy(), this.progressView);
                progressUpdateThread.start();
            }

        }
    }

    private void createProgressView() {
        if (this.progressView == null) {
            GridData gridData = new GridData();
            gridData.grabExcessHorizontalSpace = true;
            gridData.horizontalAlignment = 4;
            gridData.verticalAlignment = 4;
            this.progressView = new ProgressView(this.sShell);
            this.progressView.setLayoutData(gridData);
            this.progressView.setButtonWidthHint(this.inputFileSelection.getButtonWidthHint());
            Point preferedSize = this.progressView.computeSize(-1, -1);
            this.sShell.setSize(this.sShell.getSize().x, this.sShell.getSize().y + preferedSize.y);
        } else {
            this.progressView.reset();
        }

    }

    private void setUIEnabled(boolean enabled) {
        this.startButton.setEnabled(enabled);
        this.inputFileSelection.setEnabled(enabled);
        if (enabled) {
            this.toggleableTagEditor.setEnabled(this.getConversionStrategy().supportsTagEditor());
        } else {
            this.toggleableTagEditor.setEnabled(false);
        }

        this.optionPanel.setEnabled(enabled);
    }

    public void finishedWithError(final String errorMessage) {
        this.sShell.getDisplay().syncExec(() -> {
            MessageBox messageBox = new MessageBox(MainWindow.this.sShell, 1);
            messageBox.setText(MainWindow.this.sShell.getText());
            messageBox.setMessage(errorMessage);
            messageBox.open();
            MainWindow.this.setUIEnabled(true);
            MainWindow.this.progressView.finished();
        });
    }

    public void finished() {
        this.sShell.getDisplay().syncExec(() -> {
            MessageBox messageBox = new MessageBox(MainWindow.this.sShell, 2);
            messageBox.setText(MainWindow.this.sShell.getText());
            messageBox.setMessage(Messages.getString("MainWindow2.finished") + ".\n\n" + MainWindow.this.getConversionStrategy().getAdditionalFinishedMessage());
            messageBox.open();
            MainWindow.this.setUIEnabled(true);
            MainWindow.this.progressView.finished();
        });
    }

    public void canceled() {
        this.sShell.getDisplay().syncExec(() -> {
            MainWindow.this.setUIEnabled(true);
            MainWindow.this.progressView.finished();
        });
    }

    public static class UpdateThread extends Thread {
        public UpdateThread(Shell shell) {
            this.setDaemon(true);

            try {
                URL updateUrl = new URL("https://github.com/yermak/AudioBookConverter");
                URLConnection yc = updateUrl.openConnection();
                BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
                String inputLine = in.readLine();
                in.close();
                if ("true".equals(inputLine)) {
                    MessageBox msg = new MessageBox(shell, 194);
                    msg.setText(Messages.getString("MainWindow2.newVersion"));
                    msg.setMessage(Messages.getString("MainWindow2.aNewVersionIsAvailable"));
                    int result = msg.open();
                    if (result == 64) {
                        Program.launch("https://github.com/yermak/AudioBookConverter");
                    } else {
                        Calendar calendar = Calendar.getInstance();
                        calendar.add(5, 7);
                        AppProperties.setDateProperty("noUpdateCheckUntil", calendar.getTime());
                    }
                }
            } catch (Exception var9) {
            }

        }
    }
}
