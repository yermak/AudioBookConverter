package uk.yermak.audiobookconverter.fx;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import uk.yermak.audiobookconverter.AudiobookConverter;
import uk.yermak.audiobookconverter.book.*;
import uk.yermak.audiobookconverter.fx.util.ContextMenuBuilder;
import uk.yermak.audiobookconverter.fx.util.ContextMenuTreeTableRow;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.yermak.audiobookconverter.fx.FilesController.logger;

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

                if ((item instanceof Chapter || item instanceof Part) && item.getTotalNumbers() > 1 && getSelectionModel().getSelectedItems().size() > 1 /*&& getSelectionModel().getSelectedItems().contains(item)*/) {
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

    public void importChapterNames(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Chapter Names File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        File file = fileChooser.showOpenDialog(AudiobookConverter.getEnv().getWindow());
        
        if (file != null) {
            try {
                List<String> chapterNames = Files.readAllLines(file.toPath());
                Book book = AudiobookConverter.getContext().getBook();
                List<Chapter> chapters = book.getChapters();
                
                int minSize = Math.min(chapters.size(), chapterNames.size());
                
                for (int i = 0; i < minSize; i++) {
                    Chapter chapter = chapters.get(i);
                    String chapterName = chapterNames.get(i);
                    ChapterEditor chapterEditor = new ChapterEditor(chapter);
                    chapterEditor.uncheckAllBoxes();
                    chapter.setCustomTitle(chapterName);
                    chapter.getRenderMap().clear();  // Clear existing render map
                    chapter.getRenderMap().put("CUSTOM_TITLE", Chapter::getCustomTitle);
                }
                
                updateBookStructure();
            } catch (IOException e) {
                logger.error("Error reading chapter names file", e);
                Alert alert = new Alert(Alert.AlertType.ERROR, "Error reading chapter names file: " + e.getMessage());
                alert.showAndWait();
            }
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
        Book book = AudiobookConverter.getContext().getBook();
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
//        AudiobookConverter.getContext().getOutputParameters().updateAuto(book.getMedia());
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
        getSelectionModel().clearSelection();
    }

    void subTracks(ActionEvent actionEvent) {
        ObservableList<TreeTablePosition<Organisable, ?>> selectedCells = getSelectionModel().getSelectedCells();
        if (selectedCells.size() != 1) return;
        Organisable organisable = selectedCells.get(0).getTreeItem().getValue();

        if (organisable instanceof MediaInfo) {
            SubTracksDialog dialog = new SubTracksDialog(AudiobookConverter.getEnv().getWindow());

            Optional<Map<String, Object>> result = dialog.showAndWait();
            result.ifPresent(r -> {
                MediaInfo mediaInfo = (MediaInfo) organisable;
                extractSubtracks(mediaInfo, ((Boolean) r.get(SubTracksDialog.AUTO_CHAPTERS)), ((Integer) r.get(SubTracksDialog.INTERVAL)) * 1000, ((Boolean) r.get(SubTracksDialog.REPEAT)));
                updateBookStructure();
            });
        }
    }

    private void extractSubtracks(MediaInfo mediaInfo, Boolean wrapWithChapters, long interval, Boolean repeat) {
        double speed = AudiobookConverter.getContext().getSpeed();
        long duration = mediaInfo.getDuration();

        if (speed != 1.0) {
            interval = (long) (interval * speed);
            duration = (long) (duration * speed);
        }

        long fullTracks = duration / interval;
        List<Track> tracks = new ArrayList<>();

        if (repeat) {
            long count = fullTracks + (duration % interval > 0 ? 1 : 0);
            for (int i = 1; i <= count; i++) {
                Track track = new Track(String.valueOf(i));
                track.setTitle(mediaInfo.getTitle());
                track.setStart((i - 1) * interval);
                track.setEnd(Math.min(i * interval - 1, duration));
                tracks.add(track);
            }
        } else {
            if (interval < duration) {
                Track startTrack = new Track(String.valueOf(1));
                startTrack.setTitle(mediaInfo.getTitle());
                startTrack.setStart(0);
                startTrack.setEnd(interval);
                tracks.add(startTrack);

                Track endTrack = new Track(String.valueOf(2));
                endTrack.setTitle(mediaInfo.getTitle());
                endTrack.setStart(interval);
                endTrack.setEnd(duration);
                tracks.add(endTrack);
            }
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
