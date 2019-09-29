package uk.yermak.audiobookconverter.fx.bind;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.yermak.audiobookconverter.*;
import uk.yermak.audiobookconverter.fx.ConverterApplication;
import uk.yermak.audiobookconverter.fx.util.TextFieldValidator;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Executors;

/**
 * Created by Yermak on 04-Feb-18.
 */
public class BookInfoController {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private BookInfoDelegate delegate = new BookInfoDelegate(this);

    @FXML
    TextField title;
    @FXML
    TextField writer;
    @FXML
    TextField narrator;
    @FXML
    ComboBox<String> genre;
    @FXML
    TextField series;
    @FXML
    TextField bookNo;
    @FXML
    TextField year;
    @FXML
    TextField comment;

    @FXML
    private void initialize() {
        delegate.initialize();
        logger.debug("BookInfoController initialized");
    }
}
