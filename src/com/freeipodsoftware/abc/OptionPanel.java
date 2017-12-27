//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.freeipodsoftware.abc;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;

public class OptionPanel extends OptionPanelGui {
    public static final String OPTION_PANEL_SINGLE_OUTPUT_FILE_MODE = "optionPanel.singleOutputFileMode";
    private Set<OptionChangedListener> optionChangedListenerSet = new HashSet();
    private SelectionListener outputFileSelectionListener = new SelectionAdapter() {
        public void widgetSelected(SelectionEvent e) {
            OptionPanel.this.fireOptionChanged();
            AppProperties.setBooleanProperty("optionPanel.singleOutputFileMode", OptionPanel.this.isSingleOutputFileMode());
        }
    };

    public OptionPanel(Composite parent, int style) {
        super(parent, style);
        this.oneOutputFileOption.addSelectionListener(this.outputFileSelectionListener);
        this.oneOutputFilePerInputFileOption.addSelectionListener(this.outputFileSelectionListener);
        this.setSingleOutputFileMode(AppProperties.getBooleanProperty("optionPanel.singleOutputFileMode"));
    }

    void addOptionChangedListener(OptionChangedListener listener) {
        this.optionChangedListenerSet.add(listener);
    }

    protected void fireOptionChanged() {
        Iterator var2 = this.optionChangedListenerSet.iterator();

        while(var2.hasNext()) {
            OptionChangedListener optionChangedListener = (OptionChangedListener)var2.next();
            optionChangedListener.optionChanged();
        }

    }

    public boolean isSingleOutputFileMode() {
        return this.oneOutputFileOption.getSelection();
    }

    private void setSingleOutputFileMode(boolean singleOuputFileMode) {
        if(singleOuputFileMode) {
            this.oneOutputFileOption.setSelection(true);
        } else {
            this.oneOutputFilePerInputFileOption.setSelection(true);
        }

    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        SwtUtils.setEnabledRecursive(this, enabled);
    }
}
