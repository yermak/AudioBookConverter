package uk.yermak.audiobookconverter.fx;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.yermak.audiobookconverter.ConversionContext;
import uk.yermak.audiobookconverter.Subscriber;

import java.lang.invoke.MethodHandles;

/**
 * Created by Yermak on 06-Jan-18.
 */
public class Controller implements Subscriber {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @FXML
    ListView<ProgressComponent> progressQueue;

    @FXML
    TabPane tabs;

    @FXML
    Tab queueTab;

    @FXML
    public void initialize() {
        ConversionContext context = ConverterApplication.getContext();
        context.subscribeForStart(this);

    }

    @Override
    public void addConversionProgress(ConversionProgress conversionProgress) {
//        Platform.runLater(() -> {
        ProgressComponent progressComponent = new ProgressComponent(conversionProgress);
        progressQueue.getItems().add(0, progressComponent);
        tabs.getSelectionModel().select(queueTab);
//        });
    }
}
