package uk.yermak.audiobookconverter.fx;

import javafx.scene.control.*;
import javafx.util.Callback;

public class ContextMenuTreeTableRow<T> extends TreeTableRow<T> {

    public static <T> Callback<TreeTableView<T>, TreeTableRow<T>> forListView(ContextMenuBuilder contextMenuBuilder) {
        return forListView(contextMenuBuilder, null);
    }

    public static <T> Callback<TreeTableView<T>, TreeTableRow<T>> forListView(final ContextMenuBuilder contextMenuBuilder, final Callback<TreeTableView<T>, TreeTableRow<T>> treeTableRowFactory) {
        return listView -> {
            TreeTableRow<T> row = treeTableRowFactory == null ? new TreeTableRow<>() : treeTableRowFactory.call(listView);
            row.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
                if (isNowEmpty) {
                    row.setContextMenu(null);
                } else {
                    row.setContextMenu(contextMenuBuilder.menu(row.getTreeItem().getValue()));
                }
            });
            row.selectedProperty().addListener((observableValue, prev, current) -> {
                row.setContextMenu(contextMenuBuilder.menu(row.getTreeItem().getValue()));
            });
            return row;
        };
    }

    public ContextMenuTreeTableRow(ContextMenu contextMenu) {
        setContextMenu(contextMenu);
    }
}
