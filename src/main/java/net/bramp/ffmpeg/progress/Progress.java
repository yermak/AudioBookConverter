package net.bramp.ffmpeg.progress;

import org.apache.commons.lang3.math.Fraction;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.concurrent.TimeUnit.*;

//import static net.bramp.ffmpeg.FFmpegUtils.fromTimecode;

// TODO Change to be immutable
public class Progress {


    public enum Status {
        CONTINUE("continue"),
        END("end");

        private final String status;

        Status(String status) {
            this.status = status;
        }

        @Override
        public String toString() {
            return status;
        }

        /**
         * Returns the canonical status for this String or throws a IllegalArgumentException.
         *
         * @param status the status to convert to a Status enum.
         * @return the Status enum.
         * @throws IllegalArgumentException if the status is unknown.
         */
        public static Status of(String status) {
            for (Status s : Status.values()) {
                if (status.equalsIgnoreCase(s.status)) {
                    return s;
                }
            }

            throw new IllegalArgumentException("invalid progress status '" + status + "'");
        }
    }

    /**
     * The frame number being processed
     */
    public long frame = 0;

    /**
     * The current frames per second
     */
    public Fraction fps = Fraction.ZERO;

    /**
     * Current bitrate
     */
    public long bitrate = 0;

    /**
     * Output file size (in bytes)
     */
    public long total_size = 0;

    /**
     * Output time (in nanoseconds)
     */
    // TODO Change this to a java.time.Duration
    public long out_time_ns = 0;

    public long dup_frames = 0;

    /**
     * Number of frames dropped
     */
    public long drop_frames = 0;

    /**
     * Speed of transcoding. 1 means realtime, 2 means twice realtime.
     */
    public float speed = 0;

    /**
     * Current status, can be one of "continue", or "end"
     */
    public Status status = null;

    public Progress() {
        // Nothing
    }

    public Progress(
            long frame,
            float fps,
            long bitrate,
            long total_size,
            long out_time_ns,
            long dup_frames,
            long drop_frames,
            float speed,
            Status status) {
        this.frame = frame;
        this.fps = Fraction.getFraction(fps);
        this.bitrate = bitrate;
        this.total_size = total_size;
        this.out_time_ns = out_time_ns;
        this.dup_frames = dup_frames;
        this.drop_frames = drop_frames;
        this.speed = speed;
        this.status = status;
    }

    /**
     * Parses values from the line, into this object.
     *
     * <p>The value options are defined in ffmpeg.c's print_report function
     * https://github.com/FFmpeg/FFmpeg/blob/master/ffmpeg.c
     *
     * @param line A single line of output from ffmpeg
     * @return true if the record is finished
     */
    protected boolean parseLine(String line) {
        line = line.trim();
        if (line.isEmpty()) {
            return false; // Skip empty lines
        }

        final String[] args = line.split("=", 2);
        if (args.length != 2) {
            // invalid argument, so skip
            return false;
        }

        final String key = args[0];
        final String value = args[1];

        switch (key) {
            case "frame":
                frame = Long.parseLong(value);
                return false;

            case "fps":
                fps = Fraction.getFraction(value);
                return false;

            case "bitrate":
                if (value.equals("N/A")) {
                    bitrate = -1;
                } else {
                    bitrate = parseBitrate(value);
                }
                return false;

            case "total_size":
                if (value.equals("N/A")) {
                    total_size = -1;
                } else {
                    total_size = Long.parseLong(value);
                }
                return false;

            case "out_time_ms":
                // This is a duplicate of the "out_time" field, but expressed as a int instead of string.
                // Note this value is in microseconds, not milliseconds, and is based on AV_TIME_BASE which
                // could change.
                // out_time_ns = Long.parseLong(value) * 1000;
                return false;

            case "out_time":
                out_time_ns = fromTimecode(value);
                return false;

            case "dup_frames":
                dup_frames = Long.parseLong(value);
                return false;

            case "drop_frames":
                drop_frames = Long.parseLong(value);
                return false;

            case "speed":
                if (value.equals("N/A")) {
                    speed = -1;
                } else {
                    speed = Float.parseFloat(value.replace("x", ""));
                }
                return false;

            case "progress":
                // TODO After "end" stream is closed
                status = Status.of(value);
                return true; // The status field is always last in the record

            default:
                if (key.startsWith("stream_")) {
                    // TODO handle stream_0_0_q=0.0:
                    // stream_%d_%d_q= file_index, index, quality
                    // stream_%d_%d_psnr_%c=%2.2f, file_index, index, type{Y, U, V}, quality // Enable with
                    // AV_CODEC_FLAG_PSNR
                    // stream_%d_%d_psnr_all
                } else {
//          LOG.warn("skipping unhandled key: {} = {}", key, value);
                }

                return false; // Either way, not supported
        }
    }

    public boolean isEnd() {
        return status == Status.END;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Progress progress1 = (Progress) o;
        return frame == progress1.frame
                && bitrate == progress1.bitrate
                && total_size == progress1.total_size
                && out_time_ns == progress1.out_time_ns
                && dup_frames == progress1.dup_frames
                && drop_frames == progress1.drop_frames
                && Float.compare(progress1.speed, speed) == 0
                && Objects.equals(fps, progress1.fps)
                && Objects.equals(status, progress1.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                frame, fps, bitrate, total_size, out_time_ns, dup_frames, drop_frames, speed, status);
    }

  /*@Override
  public String toString() {
    StringUtils.toS
    return MoreObjects.toStringHelper(this)
        .add("frame", frame)
        .add("fps", fps)
        .add("bitrate", bitrate)
        .add("total_size", total_size)
        .add("out_time_ns", out_time_ns)
        .add("dup_frames", dup_frames)
        .add("drop_frames", drop_frames)
        .add("speed", speed)
        .add("status", status)
        .toString();
  }*/

    /**
     * Returns the number of nanoseconds this timecode represents. The string is expected to be in the
     * format "hour:minute:second", where second can be a decimal number.
     *
     * @param time the timecode to parse.
     * @return the number of nanoseconds.
     */
    static final Pattern TIME_REGEX = Pattern.compile("(\\d+):(\\d+):(\\d+(?:\\.\\d+)?)");

    public static long fromTimecode(String time) {
        Matcher m = TIME_REGEX.matcher(time);
        if (!m.find()) {
            throw new IllegalArgumentException("invalid time '" + time + "'");
        }

        long hours = Long.parseLong(m.group(1));
        long mins = Long.parseLong(m.group(2));
        double secs = Double.parseDouble(m.group(3));

        return HOURS.toNanos(hours) + MINUTES.toNanos(mins) + (long) (SECONDS.toNanos(1) * secs);
    }

    /**
     * Converts a string representation of bitrate to a long of bits per second
     *
     * @param bitrate in the form of 12.3kbits/s
     * @return the bitrate in bits per second or -1 if bitrate is 'N/A'
     */
    static final Pattern BITRATE_REGEX = Pattern.compile("(\\d+(?:\\.\\d+)?)kbits/s");

    public static long parseBitrate(String bitrate) {
        if ("N/A".equals(bitrate)) {
            return -1;
        }
        Matcher m = BITRATE_REGEX.matcher(bitrate);
        if (!m.find()) {
            throw new IllegalArgumentException("Invalid bitrate '" + bitrate + "'");
        }

        return (long) (Float.parseFloat(m.group(1)) * 1000);
    }


}
