package uk.yermak.audiobookconverter.fx;

import javafx.application.HostServices;
import javafx.scene.Scene;
import javafx.scene.input.Mnemonic;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

/**
 * Created by yermak on 06-Feb-18.
 */
public class JfxEnv {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Scene scene;
    private final HostServices hostServices;

    public JfxEnv(Scene scene, HostServices hostServices) {
        this.scene = scene;
        this.hostServices = hostServices;
    }

    public void setDarkMode(Boolean darkMode) {
        if (darkMode) {
            this.scene.getRoot().setStyle("-fx-base: rgba(60, 60, 60, 255);");
        } else {
            this.scene.getRoot().setStyle("");
        }
    }

    public Window getWindow() {
        return scene.getWindow();
    }

    public void showDocument(String url) {
        hostServices.showDocument(url);
    }

    public void addMnemonic(Mnemonic mnemonic) {
        scene.addMnemonic(mnemonic);
    }
}
