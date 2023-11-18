package uk.yermak.audiobookconverter.fx;

import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;

import java.util.List;


public class WizardDialog extends Dialog<Void> {

    private final TextArea hintsArea = new TextArea();
    private int currentIndex = 0;
    private List<String> hints;

    public WizardDialog(List<String> hints) {
        this.hints = hints;
        setTitle("Tip of the day...");
        initModality(Modality.APPLICATION_MODAL);
        hintsArea.setMinHeight(200);
        hintsArea.setMinWidth(500);
        hintsArea.setEditable(false);
        hintsArea.setFocusTraversable(false);
        hintsArea.setWrapText(true);
        hintsArea.setFont(javafx.scene.text.Font.font("monospaced", 15));

        DialogPane dialogPane = getDialogPane();
        dialogPane.setContent(createWizardPane());

        getDialogPane().getButtonTypes().addAll(ButtonType.PREVIOUS, ButtonType.NEXT, ButtonType.CLOSE);

        Button prevButton = (Button) getDialogPane().lookupButton(ButtonType.PREVIOUS);
        prevButton.setFocusTraversable(false);
        prevButton.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, event -> {
            event.consume();
            prev();

        });
        Button nextButton = (Button) getDialogPane().lookupButton(ButtonType.NEXT);
        nextButton.setFocusTraversable(false);
        nextButton.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, event -> {
            event.consume();
            next();
        });
    }

    private BorderPane createWizardPane() {
        BorderPane wizardPane = new BorderPane();
        wizardPane.setPadding(new javafx.geometry.Insets(20));

        hintsArea.setText(hints.get(currentIndex));

        wizardPane.setCenter(hintsArea);

        return wizardPane;
    }

    private void next() {
        if (currentIndex < hints.size() - 1) {
            currentIndex++;
        } else {
            currentIndex = 0;
        }
        hintsArea.setText(hints.get(currentIndex));
    }

    private void prev() {
        if (currentIndex > 0) {
            currentIndex--;
        } else {
            currentIndex = hints.size() - 1;
        }
        hintsArea.setText(hints.get(currentIndex));
    }
}
