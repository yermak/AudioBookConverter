package uk.yermak.audiobookconverter.fx;

import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;
import uk.yermak.audiobookconverter.ConversionContext;
import uk.yermak.audiobookconverter.Subscriber;

/**
 * Created by Yermak on 06-Jan-18.
 */
public class Controller implements Subscriber{

    @FXML
    VBox progressQueue;

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
        ProgressComponent progressComponent = new ProgressComponent();
        progressComponent.setConversionProgress(conversionProgress);
        progressQueue.getChildren().add(progressComponent);
        tabs.getSelectionModel().select(queueTab);
    }
}
