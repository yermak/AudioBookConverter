package uk.yermak.audiobookconverter;

import net.bramp.ffmpeg.progress.TcpProgressParser;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class UtilsWaitForTest {

    // A stand-in for ffmpeg: connects to the progress URL and keeps the connection open.
    private static final String HOLDER = """
            public class Holder {
                public static void main(String[] args) throws Exception {
                    try (var socket = new java.net.Socket("127.0.0.1", Integer.parseInt(args[0]))) {
                        Thread.sleep(120_000);
                    }
                }
            }
            """;

    @Test(timeOut = 30_000)
    public void stopKillsProcessSoProgressParserCanClose() throws Exception {
        File holder = File.createTempFile("Holder", ".java");
        holder.deleteOnExit();
        Files.writeString(holder.toPath(), HOLDER);
        String java = ProcessHandle.current().info().command().orElse("java");

        ConversionJob job = new ConversionJob(null, null, Map.of(), "out");
        Process process;
        try (TcpProgressParser parser = new TcpProgressParser(progress -> {})) {
            parser.start();
            process = new ProcessBuilder(java, holder.getAbsolutePath(), String.valueOf(parser.getUri().getPort())).start();
            CompletableFuture.runAsync(job::stop, CompletableFuture.delayedExecutor(3, TimeUnit.SECONDS));

            assertFalse(Utils.waitFor(process, job));
        }
        assertFalse(process.isAlive());
    }

    // A stand-in for ffmpeg asking "Overwrite? [y/N]": blocks reading stdin until end-of-input.
    private static final String PROMPT = """
            public class Prompt {
                public static void main(String[] args) throws Exception {
                    System.exit(System.in.read() == -1 ? 1 : 0);
                }
            }
            """;

    @Test(timeOut = 30_000)
    public void promptReadsEndOfInputInsteadOfHanging() throws Exception {
        File prompt = File.createTempFile("Prompt", ".java");
        prompt.deleteOnExit();
        Files.writeString(prompt.toPath(), PROMPT);
        String java = ProcessHandle.current().info().command().orElse("java");

        ConversionJob job = new ConversionJob(null, null, Map.of(), "out"); // READY: never over
        Process process = new ProcessBuilder(java, prompt.getAbsolutePath()).start();

        assertTrue(Utils.waitFor(process, job));
        assertEquals(process.exitValue(), 1);
    }
}
