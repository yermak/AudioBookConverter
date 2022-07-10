package uk.yermak.audiobookconverter.fx;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import uk.yermak.audiobookconverter.AudiobookConverter;
import uk.yermak.audiobookconverter.book.MediaInfo;
import uk.yermak.audiobookconverter.fx.util.ContextMenuBuilder;
import uk.yermak.audiobookconverter.fx.util.ContextMenuListCell;

import java.util.ArrayList;
import java.util.List;

public class FileListComponent extends ListView<MediaInfo> {

    public FileListComponent() {
        setCellFactory(ContextMenuListCell.forListView(filesContextMenuBuilder()));

        getSelectionModel().getSelectedItems().addListener((ListChangeListener<MediaInfo>) c -> {
            AudiobookConverter.getContext().getSelectedMedia().clear();
            AudiobookConverter.getContext().getSelectedMedia().addAll(c.getList());
        });


        ConversionContext context = AudiobookConverter.getContext();
        ObservableList<MediaInfo> media = context.getMedia();
        setItems(media);
        getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);


    }

    void moveFileUp(ActionEvent actionEvent) {
        ObservableList<Integer> selectedIndices = getSelectionModel().getSelectedIndices();
        if (selectedIndices.size() == 1) {
            ObservableList<MediaInfo> items = getItems();
            int selected = selectedIndices.get(0);
            if (selected > 0) {
                MediaInfo upper = items.get(selected - 1);
                MediaInfo lower = items.get(selected);
                items.set(selected - 1, lower);
                items.set(selected, upper);
                getSelectionModel().clearAndSelect(selected - 1);
            }
        }
    }

    private ContextMenuBuilder filesContextMenuBuilder() {
        return new ContextMenuBuilder<MediaInfo>() {
            @Override
            public ContextMenu menu(MediaInfo item) {
                ContextMenu contextMenu = new ContextMenu();

                if (FileListComponent.this.getItems().indexOf(item) != 0) {
                    MenuItem moveUp = new MenuItem("Move up");
                    moveUp.setOnAction(FileListComponent.this::moveFileUp);
                    contextMenu.getItems().add(moveUp);
                }

                if (FileListComponent.this.getItems().size() > FileListComponent.this.getItems().indexOf(item) + 1) {
                    MenuItem moveDown = new MenuItem("Move down");
                    moveDown.setOnAction(FileListComponent.this::moveFileDown);
                    contextMenu.getItems().add(moveDown);
                }

                if (!contextMenu.getItems().isEmpty()) {
                    contextMenu.getItems().add(new SeparatorMenuItem());
                }
                MenuItem removeMenu = new MenuItem("Remove");
                removeMenu.setOnAction(FileListComponent.this::removeFiles);
                contextMenu.getItems().add(removeMenu);
                return contextMenu;
            }
        };

    }


    void moveFileDown(ActionEvent event) {
        ObservableList<Integer> selectedIndices = getSelectionModel().getSelectedIndices();
        if (selectedIndices.size() == 1) {
            ObservableList<MediaInfo> items = getItems();
            int selected = selectedIndices.get(0);
            if (selected < items.size() - 1) {
                MediaInfo lower = items.get(selected + 1);
                MediaInfo upper = items.get(selected);
                items.set(selected, lower);
                items.set(selected + 1, upper);
                getSelectionModel().clearAndSelect(selected + 1);
            }
        }
    }

    void removeFiles(ActionEvent event) {
        ObservableList<MediaInfo> selected = getSelectionModel().getSelectedItems();
        getItems().removeAll(selected);
        //TODO assign clean up on even of media info list being empty - not explcit clean-up
//        if (getItems().isEmpty()) {
//            filesChapters.getTabs().remove(filesTab);
//        }
    }

    public void reselect() {
        ConversionContext context = AudiobookConverter.getContext();
        ObservableList<MediaInfo> selectedMedia = context.getSelectedMedia();
        ObservableList<MediaInfo> media = context.getMedia();
        List<MediaInfo> change = new ArrayList<>(selectedMedia);
        List<MediaInfo> selection = new ArrayList<>(getSelectionModel().getSelectedItems());
        if (!change.containsAll(selection) || !selection.containsAll(change)) {
            getSelectionModel().clearSelection();
            change.forEach(m ->getSelectionModel().select(media.indexOf(m)));
        }

    }
}
