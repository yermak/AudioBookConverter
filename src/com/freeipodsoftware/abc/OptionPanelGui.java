//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.freeipodsoftware.abc;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

public class OptionPanelGui extends Composite {
    private Group batchModeGroup = null;
    protected Button oneOutputFileOption = null;
    protected Button oneOutputFilePerInputFileOption = null;

    public OptionPanelGui(Composite parent, int style) {
        super(parent, style);
        this.initialize();
    }

    private void initialize() {
        this.createBatchModeGroup();
        this.setSize(new Point(423, 81));
        this.setLayout(new GridLayout());
    }

    private void createBatchModeGroup() {
        GridData gridData = new GridData();
        gridData.verticalAlignment = 1;
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = 4;
        this.batchModeGroup = new Group(this, 0);
        this.batchModeGroup.setLayout(new GridLayout());
        this.batchModeGroup.setText(Messages.getString("OptionPanelGui.options"));
        this.batchModeGroup.setLayoutData(gridData);
        this.oneOutputFileOption = new Button(this.batchModeGroup, 16);
        this.oneOutputFileOption.setText(Messages.getString("OptionPanelGui.intoOneFile"));
        this.oneOutputFilePerInputFileOption = new Button(this.batchModeGroup, 16);
        this.oneOutputFilePerInputFileOption.setText(Messages.getString("OptionPanelGui.intoSeparateFiles"));
    }
}
