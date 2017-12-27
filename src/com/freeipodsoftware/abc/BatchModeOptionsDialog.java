package com.freeipodsoftware.abc;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import java.io.File;

public class BatchModeOptionsDialog extends Dialog {
    private boolean canceled;
    private BatchModeOptionsGui gui;
    private Shell shell;
    private String folder;
    private boolean intoSameFolder;

    public BatchModeOptionsDialog(Shell parent) {
        super(parent, 65536);
        this.setText(Messages.getString("BatchModeOptionsDialog.outputOptions"));
    }

    public boolean open() {
        this.canceled = true;
        Shell parent = this.getParent();
        this.shell = new Shell(parent, 67696);
        this.shell.setText(this.getText());
        this.initializeComponents(this.shell);
        Util.centerDialog(this.getParent(), this.shell);
        this.shell.open();
        Display display = parent.getDisplay();

        while (!this.shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }

        return !this.canceled;
    }

    private void initializeComponents(final Shell shell) {
        this.gui = new BatchModeOptionsGui(shell, 0);
        shell.setLayout(new FillLayout());
        shell.setDefaultButton(this.gui.okButton);
        this.gui.folderText.setText(this.folder);
        this.gui.okButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                BatchModeOptionsDialog.this.canceled = false;
                BatchModeOptionsDialog.this.folder = BatchModeOptionsDialog.this.gui.folderText.getText();
                BatchModeOptionsDialog.this.intoSameFolder = BatchModeOptionsDialog.this.gui.sameAsInputFileRadioButton.getSelection();
                shell.close();
            }
        });
        this.gui.cancelButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                BatchModeOptionsDialog.this.canceled = true;
                shell.close();
            }
        });
        this.gui.chooseButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                DirectoryDialog dialog = new DirectoryDialog(shell);
                dialog.setMessage(Messages.getString("BatchModeOptionsDialog.chooseOuputFolder"));

                try {
                    dialog.setFilterPath(BatchModeOptionsDialog.this.gui.folderText.getText());
                } catch (Exception var4) {
                    ;
                }

                String result = dialog.open();
                if (result != null) {
                    BatchModeOptionsDialog.this.gui.folderText.setText(result);
                    BatchModeOptionsDialog.this.validateControls();
                }

            }
        });
        this.gui.differentFolderRadioButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                BatchModeOptionsDialog.this.validateControls();
            }
        });
        this.gui.sameAsInputFileRadioButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                BatchModeOptionsDialog.this.validateControls();
            }
        });
        this.gui.folderText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                BatchModeOptionsDialog.this.validateControls();
            }
        });
        shell.pack();
        shell.setSize(400, shell.getSize().y);
    }

    private void validateControls() {
        this.gui.folderText.setEnabled(this.gui.differentFolderRadioButton.getSelection());
        this.gui.chooseButton.setEnabled(this.gui.differentFolderRadioButton.getSelection());
        if (this.gui.differentFolderRadioButton.getSelection()) {
            try {
                File dir = new File(this.gui.folderText.getText());
                this.gui.okButton.setEnabled(dir.exists() && dir.isDirectory());
            } catch (Exception var2) {
                this.gui.okButton.setEnabled(false);
            }
        } else {
            this.gui.okButton.setEnabled(true);
        }

    }

    public String getFolder() {
        return this.folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public boolean isIntoSameFolder() {
        return this.intoSameFolder;
    }
}
