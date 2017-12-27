package com.freeipodsoftware.abc;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

public class BatchModeOptionsGui extends Composite {
    private Group destinationGroup = null;
    Button sameAsInputFileRadioButton = null;
    Button differentFolderRadioButton = null;
    Text folderText = null;
    Button chooseButton = null;
    private Composite buttonBarComposite = null;
    Button okButton = null;
    Button cancelButton = null;

    public BatchModeOptionsGui(Composite parent, int style) {
        super(parent, style);
        this.initialize();
    }

    private void initialize() {
        this.createDestinationGroup();
        this.createButtonBarComposite();
        this.setSize(new Point(453, 140));
        this.setLayout(new GridLayout());
    }

    private void createDestinationGroup() {
        GridData gridData2 = new GridData();
        gridData2.grabExcessHorizontalSpace = true;
        gridData2.horizontalAlignment = 4;
        GridData gridData1 = new GridData();
        gridData1.grabExcessHorizontalSpace = true;
        gridData1.horizontalAlignment = 4;
        GridData gridData = new GridData();
        gridData.horizontalSpan = 3;
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 3;
        this.destinationGroup = new Group(this, 0);
        this.destinationGroup.setText(Messages.getString("BatchModeOptionsGui.destinationForConvertedFiles"));
        this.destinationGroup.setLayoutData(gridData1);
        this.destinationGroup.setLayout(gridLayout);
        this.sameAsInputFileRadioButton = new Button(this.destinationGroup, 16);
        this.sameAsInputFileRadioButton.setText(Messages.getString("BatchModeOptionsGui.sameFolderAsInputFile"));
        this.sameAsInputFileRadioButton.setLayoutData(gridData);
        this.differentFolderRadioButton = new Button(this.destinationGroup, 16);
        this.differentFolderRadioButton.setText(Messages.getString("BatchModeOptionsGui.thisFolder"));
        this.folderText = new Text(this.destinationGroup, 2048);
        this.folderText.setLayoutData(gridData2);
        this.chooseButton = new Button(this.destinationGroup, 0);
        this.chooseButton.setText(Messages.getString("BatchModeOptionsGui.choose"));
    }

    private void createButtonBarComposite() {
        GridData gridData5 = new GridData();
        gridData5.horizontalAlignment = 4;
        GridData gridData4 = new GridData();
        gridData4.horizontalAlignment = 4;
        gridData4.widthHint = 90;
        GridData gridData3 = new GridData();
        gridData3.horizontalAlignment = 3;
        gridData3.grabExcessVerticalSpace = true;
        gridData3.verticalAlignment = 3;
        GridLayout gridLayout1 = new GridLayout();
        gridLayout1.numColumns = 2;
        gridLayout1.marginWidth = 0;
        gridLayout1.horizontalSpacing = 5;
        gridLayout1.marginHeight = 5;
        gridLayout1.makeColumnsEqualWidth = true;
        this.buttonBarComposite = new Composite(this, 0);
        this.buttonBarComposite.setLayout(gridLayout1);
        this.buttonBarComposite.setLayoutData(gridData3);
        this.okButton = new Button(this.buttonBarComposite, 0);
        this.okButton.setText(Messages.getString("BatchModeOptionsGui.ok"));
        this.okButton.setLayoutData(gridData4);
        this.cancelButton = new Button(this.buttonBarComposite, 0);
        this.cancelButton.setText(Messages.getString("BatchModeOptionsGui.cancel"));
        this.cancelButton.setLayoutData(gridData5);
    }
}
