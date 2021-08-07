package uk.yermak.audiobookconverter.fx.util;

import javafx.scene.control.ContextMenu;

public abstract class ContextMenuBuilder<T> {

    public abstract  ContextMenu menu(T item);
}
