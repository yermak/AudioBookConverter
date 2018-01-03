package com.freeipodsoftware.abc;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class InputFileSelectionGui extends Composite {
    private Label label = null;
    protected List fileList = null;
    private Composite composite = null;
    protected Button addButton = null;
    protected Button removeButton = null;
    private Composite composite1 = null;
    protected Button moveUpButton = null;
    protected Button moveDownButton = null;
    protected Button clearButton = null;

    private void createComposite() {
        GridData gridData6 = new GridData();
        gridData6.horizontalAlignment = 4;
        GridData gridData5 = new GridData();
        gridData5.horizontalAlignment = 4;
        GridData gridData2 = new GridData();
        gridData2.horizontalAlignment = 4;
        GridLayout gridLayout1 = new GridLayout();
        gridLayout1.marginHeight = 0;
        gridLayout1.marginWidth = 0;
        GridData gridData1 = new GridData();
        gridData1.grabExcessVerticalSpace = true;
        gridData1.verticalAlignment = 4;
        this.composite = new Composite(this, 0);
        this.composite.setLayoutData(gridData1);
        this.composite.setLayout(gridLayout1);
        this.addButton = new Button(this.composite, 0);
        this.addButton.setText(Messages.getString("InputFileSelectionGui.add"));
        this.addButton.setToolTipText(Messages.getString("InputFileSelectionGui.addTooltip"));
        this.addButton.setLayoutData(gridData2);
        this.removeButton = new Button(this.composite, 0);
        this.removeButton.setText(Messages.getString("InputFileSelectionGui.remove"));
        this.removeButton.setToolTipText(Messages.getString("InputFileSelectionGui.removeTooltip"));
        this.removeButton.setLayoutData(gridData5);
        this.clearButton = new Button(this.composite, 0);
        this.clearButton.setText(Messages.getString("InputFileSelectionGui.clear"));
        this.clearButton.setToolTipText(Messages.getString("InputFileSelectionGui.clearTooltip"));
        this.clearButton.setLayoutData(gridData6);
        this.createComposite1();
    }

    private void createComposite1() {
        GridData gridData4 = new GridData();
        gridData4.horizontalAlignment = 4;
        GridLayout gridLayout2 = new GridLayout();
        gridLayout2.marginHeight = 0;
        gridLayout2.marginWidth = 0;
        GridData gridData3 = new GridData();
        gridData3.grabExcessHorizontalSpace = false;
        gridData3.grabExcessVerticalSpace = true;
        this.composite1 = new Composite(this.composite, 0);
        this.composite1.setLayoutData(gridData3);
        this.composite1.setLayout(gridLayout2);
        this.moveUpButton = new Button(this.composite1, 0);
        this.moveUpButton.setText(Messages.getString("InputFileSelectionGui.moveUp"));
        this.moveUpButton.setToolTipText(Messages.getString("InputFileSelectionGui.moveUpTooltip"));
        this.moveUpButton.setLayoutData(gridData4);
        this.moveDownButton = new Button(this.composite1, 0);
        this.moveDownButton.setText(Messages.getString("InputFileSelectionGui.moveDown"));
        this.moveDownButton.setToolTipText(Messages.getString("InputFileSelectionGui.moveDownTooltip"));
    }

    public static void main(String[] args) {
        Display display = Display.getDefault();
        Shell shell = new Shell(display);
        shell.setLayout(new FillLayout());
        shell.setSize(new Point(600, 400));
        new InputFileSelectionGui(shell, 0);
        shell.open();

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }

        display.dispose();
    }

    public InputFileSelectionGui(Composite parent, int style) {
        super(parent, style);
        this.initialize();
    }

    private void initialize() {
        GridData gridData11 = new GridData();
        gridData11.horizontalSpan = 2;
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        gridLayout.makeColumnsEqualWidth = false;
        GridData gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = 4;
        gridData.verticalAlignment = 4;
        gridData.grabExcessVerticalSpace = true;
        this.label = new Label(this, 64);
        this.label.setText(Messages.getString("InputFileSelectionGui.headline"));
        this.label.setLayoutData(gridData11);
        this.fileList = new List(this, 2818);
        this.fileList.setLayoutData(gridData);
        this.setLayout(gridLayout);
        this.createComposite();
        this.setSize(new Point(545, 367));
    }
}
