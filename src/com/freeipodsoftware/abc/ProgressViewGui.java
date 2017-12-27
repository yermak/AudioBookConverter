//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.freeipodsoftware.abc;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;

public class ProgressViewGui extends Composite {
    private Label label = null;
    protected ProgressBar progressBar = null;
    private Composite composite = null;
    protected Label elapsedTimeLabel = null;
    protected Label remainingTimeLabel = null;
    private Composite composite1 = null;
    private Label outputSizeLabel = null;
    protected Label outputFileSizeValueLabel = null;
    protected Button cancelButton = null;
    protected boolean canceled;
    protected Label infoLabel = null;
    protected Button pauseButton = null;

    private void createComposite() {
        GridData gridData6 = new GridData();
        gridData6.horizontalAlignment = 4;
        gridData6.grabExcessHorizontalSpace = true;
        GridData gridData2 = new GridData();
        gridData2.grabExcessHorizontalSpace = true;
        gridData2.horizontalAlignment = 4;
        GridData gridData1 = new GridData();
        gridData1.grabExcessHorizontalSpace = true;
        gridData1.horizontalSpan = 2;
        gridData1.horizontalAlignment = 4;
        GridLayout gridLayout = new GridLayout();
        gridLayout.marginHeight = 0;
        gridLayout.numColumns = 2;
        gridLayout.marginWidth = 0;
        this.composite = new Composite(this, 0);
        this.composite.setLayout(gridLayout);
        this.composite.setLayoutData(gridData1);
        this.elapsedTimeLabel = new Label(this.composite, 0);
        this.elapsedTimeLabel.setText("Time elapsed 0:21:54");
        this.elapsedTimeLabel.setLayoutData(gridData2);
        this.remainingTimeLabel = new Label(this.composite, 131072);
        this.remainingTimeLabel.setText("Time remaining 0:53:21");
        this.remainingTimeLabel.setLayoutData(gridData6);
    }

    private void createComposite1() {
        GridData gridData5 = new GridData();
        gridData5.horizontalSpan = 2;
        gridData5.horizontalAlignment = 4;
        gridData5.grabExcessHorizontalSpace = true;
        GridData gridData4 = new GridData();
        gridData4.horizontalAlignment = 3;
        gridData4.grabExcessHorizontalSpace = false;
        GridData gridData3 = new GridData();
        gridData3.grabExcessHorizontalSpace = false;
        gridData3.horizontalSpan = 2;
        gridData3.horizontalAlignment = 4;
        GridLayout gridLayout1 = new GridLayout();
        gridLayout1.marginWidth = 0;
        gridLayout1.numColumns = 5;
        gridLayout1.marginHeight = 0;
        this.composite1 = new Composite(this, 0);
        this.composite1.setLayout(gridLayout1);
        this.composite1.setLayoutData(gridData3);
        this.outputSizeLabel = new Label(this.composite1, 0);
        this.outputSizeLabel.setText(Messages.getString("ProgressView.outputFilesize") + ":");
        this.outputFileSizeValueLabel = new Label(this.composite1, 0);
        this.outputFileSizeValueLabel.setText("---");
        this.outputFileSizeValueLabel.setLayoutData(gridData5);
        this.outputFileSizeValueLabel.setFont(new Font(Display.getDefault(), "Tahoma", 8, 1));
        this.pauseButton = new Button(this.composite1, 2);
        this.pauseButton.setText(Messages.getString("ProgressViewGui.pause"));
        this.cancelButton = new Button(this.composite1, 0);
        this.cancelButton.setText(Messages.getString("ProgressView.cancel"));
        this.cancelButton.setLayoutData(gridData4);
    }

    public static void main(String[] args) {
        Display display = Display.getDefault();
        Shell shell = new Shell(display);
        shell.setLayout(new FillLayout());
        shell.setSize(new Point(300, 200));
        new ProgressViewGui(shell, 0);
        shell.open();

        while(!shell.isDisposed()) {
            if(!display.readAndDispatch()) {
                display.sleep();
            }
        }

        display.dispose();
    }

    public ProgressViewGui(Composite parent, int style) {
        super(parent, style);
        this.initialize();
    }

    private void initialize() {
        GridData gridData11 = new GridData();
        gridData11.horizontalAlignment = 4;
        gridData11.grabExcessHorizontalSpace = true;
        GridLayout gridLayout2 = new GridLayout();
        gridLayout2.numColumns = 2;
        GridData gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalSpan = 2;
        gridData.horizontalAlignment = 4;
        this.label = new Label(this, 0);
        this.label.setText(Messages.getString("ProgressView.progress"));
        this.label.setFont(new Font(Display.getDefault(), "Tahoma", 8, 1));
        this.infoLabel = new Label(this, 131072);
        this.infoLabel.setText("Info");
        this.infoLabel.setLayoutData(gridData11);
        this.progressBar = new ProgressBar(this, 0);
        this.progressBar.setLayoutData(gridData);
        this.createComposite();
        this.setLayout(gridLayout2);
        this.createComposite1();
        this.setSize(new Point(504, 113));
    }
}
