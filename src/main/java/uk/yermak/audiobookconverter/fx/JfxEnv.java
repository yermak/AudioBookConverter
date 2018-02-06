package uk.yermak.audiobookconverter.fx;

import javafx.application.HostServices;
import javafx.scene.Scene;
import javafx.stage.Window;

/**
 * Created by yermak on 06-Feb-18.
 */
public class JfxEnv {
    private final Scene scene;
    private final HostServices hostServices;

    public JfxEnv(Scene scene, HostServices hostServices) {
        this.scene = scene;
        this.hostServices = hostServices;
    }

    public Window getWindow() {
        return scene.getWindow();
    }

    public void showDocument(String url) {
        hostServices.showDocument(url);
    }
}
