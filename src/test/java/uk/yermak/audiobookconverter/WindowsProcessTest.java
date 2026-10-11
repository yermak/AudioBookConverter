package uk.yermak.audiobookconverter;

import org.testng.annotations.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import static org.testng.Assert.assertEquals;

public class WindowsProcessTest {

    // stands in for ffmpeg, whose Windows argument parsing (CommandLineToArgvW) follows the same rules as java's launcher
    private static final String ARGS = """
            public class Args {
                public static void main(String[] args) {
                    for (String a : args) System.out.print("[" + a + "]");
                }
            }
            """;

    @Test(timeOut = 30_000)
    public void argumentsWithSpacesReachTheProcessIntact() throws Exception {
        if (!File.separator.equals("\\")) return; // Runtime.exec quoting is Windows-only
        File source = File.createTempFile("Args", ".java");
        source.deleteOnExit();
        Files.writeString(source.toPath(), ARGS);
        String java = ProcessHandle.current().info().command().orElse("java");

        Process process = Platform.WINDOWS.createProcess(List.of(java, source.getAbsolutePath(),
                "C:\\temp dir\\list.txt", "title=My Book", "comment=a b\\", "artist=Tom & Jerry", "track=3"));
        String out = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        process.waitFor();

        assertEquals(out, "[C:\\temp dir\\list.txt][title=My Book][comment=a b\\][artist=Tom & Jerry][track=3]");
    }
}
