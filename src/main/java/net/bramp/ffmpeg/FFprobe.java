package net.bramp.ffmpeg;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

/**
 * Wrapper around FFprobe
 *
 * @author bramp
 */
public class FFprobe extends FFcommon {


    static final String FFPROBE = "ffprobe";
    static final String DEFAULT_PATH = MoreObjects.firstNonNull(System.getenv("FFPROBE"), FFPROBE);

    static final Gson gson = FFmpegUtils.getGson();

    public FFprobe() {
        this(DEFAULT_PATH, new RunProcessFunction());
    }

    public FFprobe(ProcessFunction runFunction) {
        this(DEFAULT_PATH, runFunction);
    }

    public FFprobe(String path) {
        this(path, new RunProcessFunction());
    }

    public FFprobe(String path, ProcessFunction runFunction) {
        super(path, runFunction);
    }

    public FFmpegProbeResult probe(String mediaPath) throws IOException {
        return probe(mediaPath, null);
    }

    /**
     * Returns true if the binary we are using is the true ffprobe. This is to avoid conflict with
     * avprobe (from the libav project), that some symlink to ffprobe.
     *
     * @return true iff this is the official ffprobe binary.
     * @throws IOException If a I/O error occurs while executing ffprobe.
     */
    public boolean isFFprobe() throws IOException {
        return version().startsWith("ffprobe");
    }

    /**
     * Throws an exception if this is an unsupported version of ffprobe.
     *
     * @throws IllegalArgumentException if this is not the official ffprobe binary.
     * @throws IOException              If a I/O error occurs while executing ffprobe.
     */
    private void checkIfFFprobe() throws IllegalArgumentException, IOException {
        if (!isFFprobe()) {
            throw new IllegalArgumentException(
                    "This binary '" + path + "' is not a supported version of ffprobe");
        }
    }

    @Override
    public void run(List<String> args) throws IOException {
        checkIfFFprobe();
        super.run(args);
    }

    // TODO Add Probe Inputstream
    public FFmpegProbeResult probe(String mediaPath, String userAgent) throws IOException {
        checkIfFFprobe();

        ImmutableList.Builder<String> args = new ImmutableList.Builder<String>();

        // TODO Add:
        // .add("--show_packets")
        // .add("--show_frames")

        args.add(path).add("-v", "quiet");

        if (userAgent != null) {
            args.add("-user-agent", userAgent);
        }

        args.add("-print_format", "json")
                .add("-show_error")
                .add("-show_format")
                .add("-show_streams")
                .add(mediaPath);

        Process p = runFunc.run(args.build());
        try {
            Reader reader = wrapInReader(p);

            FFmpegProbeResult result = gson.fromJson(reader, FFmpegProbeResult.class);

            throwOnError(p);

            if (result == null) {
                throw new IllegalStateException("Gson returned null, which shouldn't happen :(");
            }

            return result;

        } finally {
            p.destroy();
        }
    }
}
