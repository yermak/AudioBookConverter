//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.freeipodsoftware.abc;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class AboutDialog extends Dialog {
    protected Shell sShell = null;
    protected AboutDialogGui aboutComposite;

    public AboutDialog(Shell parent) {
        super(parent);
    }

    public void open() {
        Shell parent = this.getParent();
        this.createSShell(parent);
        this.sShell.open();
        Display display = parent.getDisplay();

        while(!this.sShell.isDisposed()) {
            if(!display.readAndDispatch()) {
                display.sleep();
            }
        }

    }

    private void createSShell(Shell parent) {
        this.sShell = new Shell(parent, 67680);
        this.sShell.setText(Messages.getString("AboutDialog.caption"));
        this.sShell.setLayout(new GridLayout());
        this.aboutComposite = new AboutDialogGui(this.sShell, 0);
        this.aboutComposite.getCloseButton().addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                AboutDialog.this.sShell.close();
            }
        });
        this.sShell.setDefaultButton(this.aboutComposite.getCloseButton());
        this.sShell.pack();
    }
}
