//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.freeipodsoftware.abc;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;

public class ToggleableTagEditorGui extends Composite {
    private static final String A_TAG_CLOSING = "</a>";
    private static final String A_TAG = "<a>";
    private static final String SHOW_TAG_EDITOR = Messages.getString("ToggleableTagEditorGui.showTagEditor");
    private static final String HIDE_TAG_EDITOR = Messages.getString("ToggleableTagEditorGui.hideTagEditor");
    public static final String TOGGLEABLE_TAG_EDITOR_VISIBLE = "toggleableTagEditor.visible";
    private Link toggleLink = null;
    private TagEditor tagEditor = null;
    private Label infoLabel = null;

    public ToggleableTagEditorGui(Composite parent, int style) {
        super(parent, style);
        this.initialize();
        this.setTagEditorVisible(AppProperties.getBooleanProperty("toggleableTagEditor.visible"));
        this.layout(true);
    }

    public TagEditor getTagEditor() {
        return this.tagEditor;
    }

    private void initialize() {
        GridData gridData1 = new GridData();
        gridData1.horizontalAlignment = 1;
        GridLayout gridLayout = new GridLayout();
        gridLayout.marginHeight = 0;
        gridLayout.numColumns = 2;
        this.toggleLink = new Link(this, 0);
        this.infoLabel = new Label(this, 0);
        this.infoLabel.setText("Label");
        this.infoLabel.setLayoutData(gridData1);
        this.toggleLink.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                ToggleableTagEditorGui.this.setTagEditorVisible(!ToggleableTagEditorGui.this.tagEditor.isVisible());
                ToggleableTagEditorGui.this.getParent().layout(true);
                ToggleableTagEditorGui.this.tagEditor.setFocus();
            }
        });
        this.createTagEditor();
        this.updateToggleLinkText();
        this.setLayout(gridLayout);
        this.setSize(new Point(351, 152));
    }

    private void setTagEditorVisible(boolean visible) {
        if(visible) {
            this.tagEditor.setVisible(true);
        } else {
            this.tagEditor.setVisible(false);
        }

        this.updateToggleLinkText();
        AppProperties.setBooleanProperty("toggleableTagEditor.visible", visible);
    }

    private void createTagEditor() {
        GridData gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalSpan = 2;
        gridData.horizontalAlignment = 4;
        this.tagEditor = new TagEditor(this, 0);
        this.tagEditor.setLayoutData(gridData);
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        SwtUtils.setEnabledRecursive(this, enabled);
        this.updateToggleLinkText();
    }

    private void updateToggleLinkText() {
        if(this.isEnabled()) {
            this.infoLabel.setText("");
        } else {
            this.infoLabel.setText("(" + Messages.getString("ToggleableTagEditorGui.onlyAvailableWhenConvertingToOneFile") + ")");
        }

        if(this.tagEditor.isVisible()) {
            this.toggleLink.setText("<a>" + HIDE_TAG_EDITOR + "</a>");
        } else {
            this.toggleLink.setText("<a>" + SHOW_TAG_EDITOR + "</a>");
        }

        this.layout(true);
    }
}
