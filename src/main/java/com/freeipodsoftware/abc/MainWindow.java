package com.freeipodsoftware.abc;

import com.freeipodsoftware.abc.conversionstrategy.ConversionStrategy;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import uk.yermak.audiobookconverter.*;

import java.util.List;
import java.util.concurrent.Executors;

public class MainWindow extends MainWindowGui implements StateListener {
    private final StateDispatcher stateDispatcher = StateDispatcher.getInstance();
    private TagSuggester tagSuggester;
    private ProgressView progressView;
    private ConversionStrategy conversionStrategy;

    public static void main(String[] args) {
        Display display = Display.getDefault();
        MainWindow thisClass = new MainWindow();

        thisClass.create();
        thisClass.sShell.open();

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
        createSShell();
        startButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                MainWindow.this.startConversion();
            }
        });

        tagSuggester = new TagSuggester();
        tagSuggester.setTagEditor(toggleableTagEditor.getTagEditor());
        tagSuggester.setInputFileSelection(this.inputFileSelection);
        aboutLink2.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                MainWindow.this.showAboutDialog();
            }
        });
        updateLink.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                Program.launch("https://github.com/yermak/AudioBookConverter");
            }
        });
        websiteLink.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                Program.launch("https://github.com/yermak/AudioBookConverter");
            }
        });
        helpLink.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                Program.launch("https://github.com/yermak/AudioBookConverter");
            }
        });
        stateDispatcher.addListener(this);
    }

    private void showAboutDialog() {
        (new AboutDialog(this.sShell)).open();
    }

    private ConversionStrategy getConversionStrategy() {
        if (this.conversionStrategy != null) {
            return this.conversionStrategy;
        }
        this.conversionStrategy = optionPanel.getMode().createConvertionStrategy();
        return this.conversionStrategy;
    }

    private void startConversion() {
        List<MediaInfo> media = this.inputFileSelection.getMedia();
        getConversionStrategy().setBookInfo(this.toggleableTagEditor.getTagEditor().getAudioBookInfo());
        if (media.size() > 0) {
            if (this.getConversionStrategy().makeUserInterview(this.sShell, media.get(0).getFileName())) {
                conversionStrategy.setMedia(media);
                ProgressView progressView = createProgressView();
                JobProgress jobProgress = new JobProgress(conversionStrategy, progressView, media);

                this.setUIEnabled(false);

                this.getConversionStrategy().start(this.sShell);
                Executors.newSingleThreadExecutor().execute(jobProgress);
            }
        }
    }

    private ProgressView createProgressView() {
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
            return progressView;
        } else {
            this.progressView.reset();
            return progressView;
        }
    }

    private void setUIEnabled(boolean enabled) {
        this.startButton.setEnabled(enabled);
        this.inputFileSelection.setEnabled(enabled);
        this.optionPanel.setEnabled(enabled);
        if (enabled) {
            this.toggleableTagEditor.setEnabled(this.optionPanel.getMode().supportTags());
        } else {
            this.toggleableTagEditor.setEnabled(false);
        }
    }

    public void finishedWithError(final String errorMessage) {
        this.sShell.getDisplay().syncExec(() -> {
            MessageBox messageBox = new MessageBox(MainWindow.this.sShell, 1);
            messageBox.setText(MainWindow.this.sShell.getText());
            messageBox.setMessage(errorMessage);
            messageBox.open();
            MainWindow.this.setUIEnabled(true);
            stateDispatcher.finished();
        });
    }

    public void finished() {
        this.sShell.getDisplay().syncExec(() -> {
            MessageBox messageBox = new MessageBox(MainWindow.this.sShell, 2);
            messageBox.setText(MainWindow.this.sShell.getText());
            messageBox.setMessage(Messages.getString("MainWindow2.finished") + ".\n\n" + MainWindow.this.getConversionStrategy().getAdditionalFinishedMessage());
            messageBox.open();
            MainWindow.this.setUIEnabled(true);
        });
        conversionStrategy = null;
    }

    public void canceled() {
        this.sShell.getDisplay().syncExec(() -> {
            MainWindow.this.setUIEnabled(true);
        });
        conversionStrategy = null;
    }

    @Override
    public void paused() {

    }

    @Override
    public void resumed() {

    }

    @Override
    public void fileListChanged() {

    }

    @Override
    public void modeChanged(ConversionMode mode) {
        toggleableTagEditor.setEnabled(optionPanel.getMode().supportTags());
    }

}
