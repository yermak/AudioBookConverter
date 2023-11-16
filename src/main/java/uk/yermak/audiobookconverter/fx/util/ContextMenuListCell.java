package uk.yermak.audiobookconverter.fx.util;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

public class ContextMenuListCell<T> extends ListCell<T> {

    public static <T> Callback<ListView<T>, ListCell<T>> forListView(ContextMenuBuilder contextMenuBuilder) {
        return forListView(contextMenuBuilder, null);
    }

    public static <T> Callback<ListView<T>, ListCell<T>> forListView(final ContextMenuBuilder contextMenuBuilder, final Callback<ListView<T>, ListCell<T>> cellFactory) {
        return listView -> {
            ListCell<T> cell = cellFactory == null ? new DefaultListCell<>() : cellFactory.call(listView);
            cell.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
                if (isNowEmpty) {
                    cell.setContextMenu(null);
                } else {
                    cell.setContextMenu(contextMenuBuilder.menu(cell.getItem()));
                }
            });
            return cell;
        };
    }

    public ContextMenuListCell(ContextMenu contextMenu) {
        setContextMenu(contextMenu);
    }
}
