package uk.yermak.audiobookconverter.fx;

import javafx.scene.control.*;
import javafx.util.Callback;

public class ContextMenuTreeTableRow<T> extends TreeTableRow<T> {

    public static <T> Callback<TreeTableView<T>, TreeTableRow<T>> forListView(ContextMenu contextMenu) {
        return forListView(contextMenu, null);
    }

    public static <T> Callback<TreeTableView<T>, TreeTableRow<T>> forListView(final ContextMenu contextMenu, final Callback<TreeTableView<T>, TreeTableRow<T>> treeTableRowFactory) {
        return listView -> {
            TreeTableRow<T> row = treeTableRowFactory == null ? new TreeTableRow<>() : treeTableRowFactory.call(listView);
            row.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
                if (isNowEmpty) {
                    row.setContextMenu(null);
                } else {
                    row.setContextMenu(contextMenu);
                }
            });
            return row;
        };
    }

    public ContextMenuTreeTableRow(ContextMenu contextMenu) {
        setContextMenu(contextMenu);
    }
}
