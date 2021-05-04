package uk.yermak.audiobookconverter.fx;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.util.Pair;
import uk.yermak.audiobookconverter.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BookStructureComponent extends TreeTableView<Organisable> {

    public BookStructureComponent() {
        setRowFactory(ContextMenuTreeTableRow.forListView(buildChaptersContextMenu()));

    }

    private ContextMenuBuilder buildChaptersContextMenu() {

        return new ContextMenuBuilder<Organisable>() {
            @Override
            public ContextMenu menu(Organisable item) {
                ContextMenu contextMenu = new ContextMenu();
                if (item instanceof Chapter && getSelectionModel().getSelectedItems().size() == 1) {
                    MenuItem edit = new MenuItem("Edit");
                    edit.setOnAction(BookStructureComponent.this::editChapter);
                    contextMenu.getItems().add(edit);
                }

                if (!contextMenu.getItems().isEmpty() && !(contextMenu.getItems().get(contextMenu.getItems().size() - 1) instanceof SeparatorMenuItem)) {
                    contextMenu.getItems().add(new SeparatorMenuItem());
                }

                if (item.getTotalNumbers() > 1 && item.getNumber() > 1 && getSelectionModel().getSelectedItems().size() == 1) {
                    MenuItem moveUp = new MenuItem("Move up");
                    moveUp.setOnAction(BookStructureComponent.this::moveChapterUp);
                    contextMenu.getItems().add(moveUp);
                }

                if (item.getTotalNumbers() > 1 && item.getNumber() < item.getTotalNumbers() && getSelectionModel().getSelectedItems().size() == 1) {
                    MenuItem moveDown = new MenuItem("Move down");
                    moveDown.setOnAction(BookStructureComponent.this::moveChapterDown);
                    contextMenu.getItems().add(moveDown);
                }

                if (!contextMenu.getItems().isEmpty() && !(contextMenu.getItems().get(contextMenu.getItems().size() - 1) instanceof SeparatorMenuItem)) {
                    contextMenu.getItems().add(new SeparatorMenuItem());
                }

                if (item instanceof MediaInfo && item.getTotalNumbers() > 1 && item.getNumber() > 1 && getSelectionModel().getSelectedItems().size() == 1) {
                    MenuItem split = new MenuItem("Split to new chapter");
                    split.setOnAction(BookStructureComponent.this::split);
                    contextMenu.getItems().add(split);
                }

                if (item instanceof Chapter && item.getTotalNumbers() > 1 && getSelectionModel().getSelectedItems().size() > 1 /*&& getSelectionModel().getSelectedItems().contains(item)*/) {
                    MenuItem combine = new MenuItem("Combine");
                    combine.setOnAction(BookStructureComponent.this::combineChapters);
                    contextMenu.getItems().add(combine);
                }

                if (item instanceof Chapter && item.getTotalNumbers() > 1 && getSelectionModel().getSelectedItems().size() == 1) {
                    MenuItem split = new MenuItem("Split to new part");
                    split.setOnAction(BookStructureComponent.this::split);
                    contextMenu.getItems().add(split);
                }

                if (!contextMenu.getItems().isEmpty() && !(contextMenu.getItems().get(contextMenu.getItems().size() - 1) instanceof SeparatorMenuItem)) {
                    contextMenu.getItems().add(new SeparatorMenuItem());
                }

                if (item instanceof MediaInfo && getSelectionModel().getSelectedItems().size() == 1) {
                    MenuItem subTracks = new MenuItem("Sub-tracks");
                    subTracks.setOnAction(BookStructureComponent.this::subTracks);
                    contextMenu.getItems().add(subTracks);
                }

                if (!contextMenu.getItems().isEmpty() && !(contextMenu.getItems().get(contextMenu.getItems().size() - 1) instanceof SeparatorMenuItem)) {
                    contextMenu.getItems().add(new SeparatorMenuItem());
                }

                MenuItem removeMenu = new MenuItem("Remove");
                removeMenu.setOnAction(BookStructureComponent.this::removeChapters);
                contextMenu.getItems().add(removeMenu);
                return contextMenu;
            }
        };

    }

    public void editChapter(ActionEvent actionEvent) {
        ObservableList<TreeTablePosition<Organisable, ?>> selectedCells = getSelectionModel().getSelectedCells();
        if (selectedCells.size() != 1) return;
        Organisable organisable = selectedCells.get(0).getTreeItem().getValue();
        if (organisable instanceof Chapter) {
            new ChapterEditor((Chapter) organisable).editChapter();
            refresh();
        }
    }


    void moveChapterUp(ActionEvent event) {
        ObservableList<TreeTablePosition<Organisable, ?>> selectedCells = getSelectionModel().getSelectedCells();
        if (selectedCells.size() == 1) {
            Organisable organisable = selectedCells.get(0).getTreeItem().getValue();
            organisable.moveUp();
            Platform.runLater(this::updateBookStructure);
        }
    }

    void updateBookStructure() {
        Book book = ConverterApplication.getContext().getBook();
        getRoot().getChildren().clear();
        book.getParts().forEach(p -> {
            TreeItem<Organisable> partItem = new TreeItem<>(p);
            getRoot().getChildren().add(partItem);
            p.getChapters().forEach(c -> {
                TreeItem<Organisable> chapterItem = new TreeItem<>(c);
                partItem.getChildren().add(chapterItem);
                c.getMedia().forEach(m -> chapterItem.getChildren().add(new TreeItem<>(m)));
            });
        });
        getRoot().getChildren().forEach(t -> t.setExpanded(true));
        refresh();
        ConverterApplication.getContext().getOutputParameters().updateAuto(book.getMedia());
    }

    void moveChapterDown(ActionEvent event) {
        ObservableList<TreeTablePosition<Organisable, ?>> selectedCells = getSelectionModel().getSelectedCells();
        if (selectedCells.size() == 1) {
            Organisable organisable = selectedCells.get(0).getTreeItem().getValue();
            organisable.moveDown();
            Platform.runLater(this::updateBookStructure);
        }
    }

    void split(ActionEvent event) {
        ObservableList<TreeTablePosition<Organisable, ?>> selectedCells = getSelectionModel().getSelectedCells();
        if (selectedCells.size() != 1) return;
        Organisable organisable = selectedCells.get(0).getTreeItem().getValue();
        boolean split = organisable.split();
        if (split)
            updateBookStructure();
    }

    void combineChapters(ActionEvent event) {
        ObservableList<TreeTablePosition<Organisable, ?>> selectedCells = getSelectionModel().getSelectedCells();
        if (selectedCells.isEmpty()) return;
        List<Part> partMergers = selectedCells.stream().map(s -> s.getTreeItem().getValue()).filter(v -> (v instanceof Part)).map(c -> (Part) c).collect(Collectors.toList());
        if (partMergers.size() > 1) {
            Part recipient = partMergers.remove(0);
            recipient.combine(partMergers);
        } else {
            List<Chapter> chapterMergers = selectedCells.stream().map(s -> s.getTreeItem().getValue()).filter(v -> (v instanceof Chapter)).map(c -> (Chapter) c).collect(Collectors.toList());
            if (chapterMergers.size() > 1) {
                Chapter recipient = chapterMergers.remove(0);
                recipient.combine(chapterMergers);
            }
        }
        updateBookStructure();
    }

    private void subTracks(ActionEvent actionEvent) {
        ObservableList<TreeTablePosition<Organisable, ?>> selectedCells = getSelectionModel().getSelectedCells();
        if (selectedCells.size() != 1) return;
        Organisable organisable = selectedCells.get(0).getTreeItem().getValue();

        if (organisable instanceof MediaInfo) {
            SubTracksDialog dialog = new SubTracksDialog(ConverterApplication.getEnv().getWindow());

            Optional<Pair<Integer, Boolean>> result = dialog.showAndWait();
            result.ifPresent(r -> {
                MediaInfo mediaInfo = (MediaInfo) organisable;
                extractSubtracks(mediaInfo, r.getValue(), r.getKey() * 60000);
                updateBookStructure();
            });
        }
    }

    private void extractSubtracks(MediaInfo mediaInfo, Boolean wrapWithChapters, long interval) {
        double speed = ConverterApplication.getContext().getSpeed();
        long duration = mediaInfo.getDuration();

        if (speed != 1.0) {
            interval = (long) (interval * speed);
            duration = (long) (duration * speed);
        }

        long fullTracks = duration / interval;
        List<Track> tracks = new ArrayList<>();
        for (int i = 1; i <= fullTracks + (duration % interval > 0 ? 1 : 0); i++) {
            Track track = new Track(String.valueOf(i));
            track.setTitle(mediaInfo.getTitle());
            track.setStart((i - 1) * interval);
            track.setEnd(Math.min(i * interval - 1, duration));
            tracks.add(track);
        }

        if (wrapWithChapters) {
            Part part = mediaInfo.getChapter().getPart();
            part.replaceMediaChapterByTracksChapters(mediaInfo, tracks);
        } else {
            Chapter chapter = mediaInfo.getChapter();
            chapter.replaceMediaWithTracks(mediaInfo, tracks);
        }
    }


    void removeChapters(ActionEvent event) {
        ObservableList<TreeTablePosition<Organisable, ?>> selectedCells = getSelectionModel().getSelectedCells();
        for (TreeTablePosition<Organisable, ?> selectedCell : selectedCells) {
            Organisable organisable = selectedCell.getTreeItem().getValue();
            organisable.remove();
        }
        Platform.runLater(() -> {
            updateBookStructure();
            //TODO temp hack - can't selection on previous row causes NPE on next removal...
            getSelectionModel().clearSelection();
            /*if (getRoot().getChildren().isEmpty()) {
                filesController.clear(event);
            }*/
        });
    }
}
