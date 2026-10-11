package uk.yermak.audiobookconverter;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.testng.Assert.assertEquals;

public class PlatformTest {

    @AfterMethod
    public void clearLauncher() {
        System.clearProperty("jpackage.app-path");
    }

    @Test
    public void linuxPathsResolveFromLauncherDir() throws IOException {
        Path root = Files.createTempDirectory("abc-linux");
        System.setProperty("jpackage.app-path", root.resolve("bin/AudioBookConverter").toString());

        assertEquals(Platform.LINUX.getConfigFilePath().getCanonicalFile(), root.resolve("lib/app/path.properties").toFile().getCanonicalFile());
        assertEquals(new File(Platform.LINUX.getAppPath() + "../lib/app/external/ffmpeg").getCanonicalFile(),
                root.resolve("lib/app/external/ffmpeg").toFile().getCanonicalFile());
    }

    @Test
    public void macConfigResolvesFromBundle() throws IOException {
        Path root = Files.createTempDirectory("abc-mac");
        System.setProperty("jpackage.app-path", root.resolve("AudioBookConverter.app/Contents/MacOS/AudioBookConverter").toString());

        assertEquals(Platform.MAC.getConfigFilePath().getCanonicalFile(),
                root.resolve("AudioBookConverter.app/Contents/app/path.properties").toFile().getCanonicalFile());
        assertEquals(Platform.MAC.getAppPath(), "");
    }

    @Test
    public void withoutLauncherPathsStayRelativeToWorkingDir() {
        assertEquals(Platform.LINUX.getConfigFilePath().getPath(), new File("../lib/app/path.properties").getPath());
        assertEquals(Platform.LINUX.getAppPath(), "");
    }
}
