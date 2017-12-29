package com.freeipodsoftware.abc;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class SwtUtils {

    public static void setEnabledRecursive(Composite composite, boolean enabled) {
        Control[] children = composite.getChildren();

        for (Control child : children) {
            child.setEnabled(enabled);
            if (child instanceof Composite) {
                Composite subComposite = (Composite) child;
                setEnabledRecursive(subComposite, enabled);
            }
        }

    }
}
