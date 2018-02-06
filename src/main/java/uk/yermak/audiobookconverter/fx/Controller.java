package uk.yermak.audiobookconverter.fx;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import uk.yermak.audiobookconverter.ConvertsionContext;

/**
 * Created by Yermak on 06-Jan-18.
 */
public class Controller {

    @FXML
    public void initialize() {

    }

    public void start(ActionEvent actionEvent) {
        ConvertsionContext context = ConverterApplication.getContext();
        System.out.println("context = " + context);
    }
}
