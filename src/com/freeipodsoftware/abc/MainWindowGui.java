//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.freeipodsoftware.abc;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;

public class MainWindowGui {
    protected Shell sShell = null;
    private Composite composite;
    private Label label;
    private Composite composite2;
    protected Link websiteLink;
    protected Link aboutLink2;
    protected Link helpLink;
    protected Link updateLink;
    protected InputFileSelection inputFileSelection;
    protected ToggleableTagEditorGui toggleableTagEditor;
    private Composite startButtonComposite;
    protected Button startButton = null;
    protected OptionPanel optionPanel = null;

    public MainWindowGui() {
    }

    private void createStartButtonComposite() {
        GridData gridData2 = new GridData();
        gridData2.grabExcessHorizontalSpace = true;
        gridData2.horizontalAlignment = 2;
        GridData gridData11 = new GridData();
        gridData11.widthHint = 160;
        this.startButton = new Button(this.startButtonComposite, 0);
        this.startButton.setText(Messages.getString("MainWindow2.startConversion"));
        this.startButton.setLayoutData(gridData11);
        this.startButton.setFont(new Font(Display.getDefault(), "Tahoma", 8, 1));
    }

    private void createOptionPanel() {
        GridData gridData10 = new GridData();
        gridData10.horizontalAlignment = 4;
        this.optionPanel = new OptionPanel(this.sShell, 0);
        this.optionPanel.setLayoutData(gridData10);
    }

    public static void main(String[] args) {
        Display display = Display.getDefault();
        MainWindowGui thisClass = new MainWindowGui();
        thisClass.createSShell();
        thisClass.sShell.open();

        while(!thisClass.sShell.isDisposed()) {
            if(!display.readAndDispatch()) {
                display.sleep();
            }
        }

        display.dispose();
    }

    protected void createSShell() {
        this.sShell = new Shell();
        this.sShell.setText(Messages.getString("MainWindow2.programName"));
        this.sShell.setSize(new Point(621, 582));
        GridLayout gridLayout1 = new GridLayout();
        gridLayout1.marginHeight = 3;
        gridLayout1.verticalSpacing = 0;
        gridLayout1.marginWidth = 0;
        this.sShell.setLayout(gridLayout1);
        this.createComposite();
        this.createInputFileSelection();
        this.createOptionPanel();
        this.createToggleableTagEditor();
        this.createComposite1();
    }

    private void createComposite1() {
        GridLayout gridLayout3 = new GridLayout();
        gridLayout3.marginHeight = 5;
        gridLayout3.horizontalSpacing = 1;
        gridLayout3.numColumns = 1;
        GridData gridData1 = new GridData();
        gridData1.grabExcessHorizontalSpace = true;
        gridData1.horizontalAlignment = 2;
        this.startButtonComposite = new Composite(this.sShell, 0);
        this.startButtonComposite.setLayoutData(gridData1);
        this.startButtonComposite.setLayout(gridLayout3);
        this.createStartButtonComposite();
    }

    private void createToggleableTagEditor() {
        GridData gridData9 = new GridData();
        gridData9.grabExcessHorizontalSpace = true;
        gridData9.horizontalAlignment = 4;
        this.toggleableTagEditor = new ToggleableTagEditorGui(this.sShell, 0);
        this.toggleableTagEditor.setLayoutData(gridData9);
    }

    private void createInputFileSelection() {
        GridData gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = 4;
        gridData.verticalAlignment = 4;
        gridData.grabExcessVerticalSpace = true;
        this.inputFileSelection = new InputFileSelection(this.sShell, 0);
        this.inputFileSelection.setToolTipText("");
        this.inputFileSelection.setLayoutData(gridData);
    }

    private void createComposite() {
        GridData gridData4 = new GridData();
        gridData4.grabExcessHorizontalSpace = true;
        gridData4.verticalAlignment = 2;
        GridLayout gridLayout = new GridLayout();
        gridLayout.marginHeight = 0;
        gridLayout.numColumns = 2;
        gridLayout.marginWidth = 5;
        gridLayout.numColumns = 2;
        GridData gridData6 = new GridData();
        gridData6.grabExcessHorizontalSpace = true;
        gridData6.horizontalAlignment = 4;
        this.composite = new Composite(this.sShell, 0);
        this.composite.setLayout(gridLayout);
        this.composite.setLayoutData(gridData6);
        this.label = new Label(this.composite, 0);
        this.label.setText(Messages.getString("MainWindow2.programName"));
        this.label.setLayoutData(gridData4);
        this.label.setFont(new Font(Display.getDefault(), "Tahoma", 14, 1));
        this.createComposite2();
    }

    private void createComposite2() {
        GridData gridData8 = new GridData();
        gridData8.grabExcessHorizontalSpace = true;
        gridData8.horizontalAlignment = 3;
        GridData gridData7 = new GridData();
        gridData7.grabExcessHorizontalSpace = true;
        gridData7.horizontalAlignment = 1;
        GridData gridData5 = new GridData();
        gridData5.grabExcessHorizontalSpace = true;
        gridData5.horizontalAlignment = 1;
        GridData gridData3 = new GridData();
        gridData3.grabExcessHorizontalSpace = true;
        gridData3.horizontalAlignment = 3;
        GridLayout gridLayout2 = new GridLayout();
        gridLayout2.numColumns = 2;
        gridLayout2.marginWidth = 5;
        gridLayout2.horizontalSpacing = 5;
        gridLayout2.verticalSpacing = 3;
        gridLayout2.marginHeight = 3;
        this.composite2 = new Composite(this.composite, 0);
        this.composite2.setLayout(gridLayout2);
        this.websiteLink = new Link(this.composite2, 0);
        this.websiteLink.setText("<a>" + Messages.getString("MainWindow2.website") + "</a>");
        this.websiteLink.setLayoutData(gridData8);
        this.aboutLink2 = new Link(this.composite2, 0);
        this.aboutLink2.setText("<a>" + Messages.getString("MainWindow2.aboutThisSoftware") + "</a>");
        this.aboutLink2.setLayoutData(gridData5);
        this.helpLink = new Link(this.composite2, 0);
        this.helpLink.setText("<a>" + Messages.getString("MainWindow2.help") + "</a>");
        this.helpLink.setLayoutData(gridData3);
        this.updateLink = new Link(this.composite2, 0);
        this.updateLink.setText("<a>" + Messages.getString("MainWindow2.checkForUpdates") + "</a>");
        this.updateLink.setLayoutData(gridData7);
    }
}
