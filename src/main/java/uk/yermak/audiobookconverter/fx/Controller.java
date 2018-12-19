package uk.yermak.audiobookconverter.fx;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import uk.yermak.audiobookconverter.ConversionContext;
import uk.yermak.audiobookconverter.Subscriber;

/**
 * Created by Yermak on 06-Jan-18.
 */
public class Controller implements Subscriber {

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
