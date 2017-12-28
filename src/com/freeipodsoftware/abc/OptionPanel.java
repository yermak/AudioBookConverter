package com.freeipodsoftware.abc;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import uk.yermak.audiobookconverter.ConversionMode;

import java.util.HashSet;
import java.util.Set;

public class OptionPanel extends OptionPanelGui {
    private static final String OPTION_PANEL_CONVERSION_MODE = "optionPanel.conversionMode";
    private Set<OptionChangedListener> optionChangedListenerSet = new HashSet<>();
    private ConversionMode mode = ConversionMode.SINGLE;

    public OptionPanel(Composite parent) {
        super(parent, 0);
        this.oneOutputFileOption.addSelectionListener(new ModeSelectionAdapter(ConversionMode.SINGLE));
        this.oneOutputFilePerInputFileOption.addSelectionListener(new ModeSelectionAdapter(ConversionMode.BATCH));
        this.oneOutputFileParallelProcessingFileOption.addSelectionListener(new ModeSelectionAdapter(ConversionMode.PARALLEL));
        this.setConversionMode(ConversionMode.valueOf(AppProperties.getProperty(OPTION_PANEL_CONVERSION_MODE, ConversionMode.SINGLE.toString())));
    }

    void addOptionChangedListener(OptionChangedListener listener) {
        this.optionChangedListenerSet.add(listener);
    }

    private void fireOptionChanged() {
        for (OptionChangedListener optionChangedListener : optionChangedListenerSet) {
            optionChangedListener.optionChanged();
        }
    }

    public ConversionMode getMode() {
        return mode;
    }

    private void setConversionMode(ConversionMode mode) {
        this.mode = mode;
        switch (mode) {
            case SINGLE:
                this.oneOutputFileOption.setSelection(true);
                break;
            case BATCH:
                this.oneOutputFilePerInputFileOption.setSelection(true);
                break;
            case PARALLEL:
                this.oneOutputFileParallelProcessingFileOption.setSelection(true);
                break;
        }
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        SwtUtils.setEnabledRecursive(this, enabled);
    }

    private class ModeSelectionAdapter extends SelectionAdapter {
        private ConversionMode mode;

        public ModeSelectionAdapter(ConversionMode mode) {
            this.mode = mode;
        }

        public void widgetSelected(SelectionEvent e) {
            OptionPanel.this.mode = this.mode;
            AppProperties.setProperty(OPTION_PANEL_CONVERSION_MODE, mode.toString());
            OptionPanel.this.fireOptionChanged();
        }
    }
}
