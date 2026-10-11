package uk.yermak.audiobookconverter;

import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.prefs.Preferences;

import static org.testng.Assert.assertEquals;

public class SettingsTest {

    private static final File HOME = new File(System.getProperty("user.home"));

    @Test
    public void missingFoldersFallBackWithoutNpe() throws IOException {
        File existing = Files.createTempDirectory("abc-out").toFile();

        assertEquals(new Settings().setOutputFolder(existing.getPath()).getOutputFolder(), existing);
        assertEquals(new Settings().setOutputFolder(new File(existing, "gone").getPath()).getOutputFolder(), existing);
        // no parent: an empty path, or the root of an unplugged drive
        assertEquals(new Settings().setOutputFolder("").getOutputFolder(), HOME);
        assertEquals(new Settings().setSourceFolder("").getSourceFolder(), HOME);
        assertEquals(new Settings().setSourceFolder(null).getSourceFolder(), HOME);
    }

    @Test
    public void valuesLongerThanPreferencesLimitAreStoredInParts() throws Exception {
        Preferences node = Preferences.userRoot().node("uk/yermak/audiobookconverter/test-" + System.nanoTime());
        try {
            String longValue = "x".repeat(Preferences.MAX_VALUE_LENGTH * 2 + 5);
            Settings.write(node, "settings", longValue);
            assertEquals(Settings.read(node, "settings"), longValue);

            Settings.write(node, "settings", "short"); // leftover parts of the longer value go away
            assertEquals(Settings.read(node, "settings"), "short");
            assertEquals(node.keys().length, 1);

            Settings.remove(node, "settings");
            assertEquals(Settings.read(node, "settings"), null);
        } finally {
            node.removeNode();
        }
    }

    @Test
    public void unpluggedDriveRootFallsBackToHome() {
        if (!File.separator.equals("\\")) return; // drive roots are a Windows thing
        for (char drive = 'Z'; drive >= 'D'; drive--) {
            File root = new File(drive + ":\\");
            if (!root.exists()) {
                assertEquals(new Settings().setOutputFolder(root.getPath()).getOutputFolder(), HOME);
                return;
            }
        }
    }
}
