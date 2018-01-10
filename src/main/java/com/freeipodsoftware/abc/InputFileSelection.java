package com.freeipodsoftware.abc;

import org.eclipse.swt.dnd.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import uk.yermak.audiobookconverter.MediaInfo;
import uk.yermak.audiobookconverter.MediaLoader;
import uk.yermak.audiobookconverter.StateDispatcher;

import java.util.ArrayList;
import java.util.List;

public class InputFileSelection extends InputFileSelectionGui {
    private final StateDispatcher stateDispatcher = StateDispatcher.getInstance();
    private String lastFolder;
    private List<MediaInfo> media = new ArrayList<>();


    public InputFileSelection(Composite parent) {
        super(parent, 0);
        addButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                InputFileSelection.this.addInputFile();
            }
        });
        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                InputFileSelection.this.removeInputFiles();
            }
        });
        clearButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                InputFileSelection.this.removeAllInputFiles();
            }
        });
        moveUpButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                InputFileSelection.this.moveUp();
            }
        });
        moveDownButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                InputFileSelection.this.moveDown();
            }
        });
        createDropTarget();
        fileListControl.addKeyListener(new InputFileSelection.MyKeyListener());
        addButton.setFocus();

    }

    private void createDropTarget() {
        DropTarget target = new DropTarget(this.fileListControl, 19);
        target.setTransfer(new Transfer[]{FileTransfer.getInstance(), TextTransfer.getInstance()});
        target.addDropListener(new DropTargetAdapter() {
            public void drop(DropTargetEvent event) {
                if (FileTransfer.getInstance().isSupportedType(event.currentDataType)) {
                    String[] files = (String[]) event.data;

                    for (String file : files) {
                        InputFileSelection.this.fileListControl.add(file);
                    }
                    stateDispatcher.fileListChanged();
                }
            }
        });
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        SwtUtils.setEnabledRecursive(this, enabled);
    }

    public int getButtonWidthHint() {
        return addButton.getSize().x;
    }

    private void addInputFile() {
        FileDialog fileDialog = new FileDialog(this.getShell(), 4098);
        if (lastFolder != null) {
            fileDialog.setFileName(lastFolder);
        }

        fileDialog.setFilterNames(new String[]{Messages.getString("InputFileSelection.mp3Files"), Messages.getString("InputFileSelection.allFiles")});
        fileDialog.setFilterExtensions(new String[]{"*.mp3", "*.*"});
        String firstFile = fileDialog.open();
        if (firstFile != null) {
            this.lastFolder = firstFile;
            List<MediaInfo> addedMedia = loadMediaFiles(fileDialog.getFilterPath(), fileDialog.getFileNames());
            media.addAll(addedMedia);
            addedMedia.forEach(m -> fileListControl.add(m.getFileName()));
            stateDispatcher.fileListChanged();
        }

    }

    private List<MediaInfo> loadMediaFiles(String directory, String[] fileNames) {
        List<String> files = new ArrayList<>();
        for (String fileName : fileNames) {
            String fullName = directory + System.getProperty("file.separator") + fileName;
            files.add(fullName);
        }
        List<MediaInfo> addedMedia = new MediaLoader(files).loadMediaInfo();
        return addedMedia;
    }

    private void removeAllInputFiles() {
        fileListControl.removeAll();
        media.clear();
        stateDispatcher.fileListChanged();
    }


    private void removeInputFiles() {
        int[] selectionIndices = fileListControl.getSelectionIndices();
        for (int i = 0; i < selectionIndices.length; i++) {
            int selectionIndex = selectionIndices[i];
            String item = fileListControl.getItem(selectionIndex);
            media.removeIf(mi -> item.equals(mi.getFileName()));
        }
        this.fileListControl.remove(selectionIndices);
        stateDispatcher.fileListChanged();
    }

    private void moveDown() {
        if (this.fileListControl.getSelectionCount() == 1) {
            int selectionIndex = fileListControl.getSelectionIndex();
            MediaInfo mediaInfo1 = media.get(selectionIndex);
            MediaInfo mediaInfo2 = media.get(selectionIndex + 1);
            media.set(selectionIndex, mediaInfo2);
            media.set(selectionIndex + 1, mediaInfo1);

            if (selectionIndex < fileListControl.getItemCount() - 1) {
                fileListControl.add(fileListControl.getItem(selectionIndex), selectionIndex + 2);
                fileListControl.remove(selectionIndex);
                fileListControl.setSelection(selectionIndex + 1);
                stateDispatcher.fileListChanged();
            }
        }

    }

    private void moveUp() {
        if (this.fileListControl.getSelectionCount() == 1) {
            int selectionIndex = fileListControl.getSelectionIndex();
            if (selectionIndex > 0) {
                MediaInfo mediaInfo1 = media.get(selectionIndex);
                MediaInfo mediaInfo2 = media.get(selectionIndex - 1);
                media.set(selectionIndex, mediaInfo2);
                media.set(selectionIndex - 1, mediaInfo1);

                fileListControl.add(fileListControl.getItem(selectionIndex), selectionIndex - 1);
                fileListControl.remove(selectionIndex + 1);
                fileListControl.setSelection(selectionIndex - 1);
                stateDispatcher.fileListChanged();
            }
        }

    }


    public List<MediaInfo> getMedia() {
        return media;
    }

    class MyKeyListener extends KeyAdapter implements KeyListener {
        MyKeyListener() {
        }

        public void keyPressed(KeyEvent e) {
            super.keyPressed(e);
            if (e.keyCode == 127) {
                InputFileSelection.this.removeInputFiles();
            } else if (e.keyCode == 97 && e.stateMask == 262144) {
                InputFileSelection.this.fileListControl.selectAll();
            }

        }
    }
}
