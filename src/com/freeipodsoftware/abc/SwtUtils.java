package com.freeipodsoftware.abc;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class SwtUtils {
    public SwtUtils() {
    }

    public static void setEnabledRecursive(Composite composite, boolean enabled) {
        Control[] children = composite.getChildren();

        for(int i = 0; i < children.length; ++i) {
            children[i].setEnabled(enabled);
            if(children[i] instanceof Composite) {
                Composite subComposite = (Composite)children[i];
                setEnabledRecursive(subComposite, enabled);
            }
        }

    }
}
