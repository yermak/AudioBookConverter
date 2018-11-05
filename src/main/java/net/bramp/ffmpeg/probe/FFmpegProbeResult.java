package net.bramp.ffmpeg.probe;

import com.google.common.collect.ImmutableList;

import java.util.List;

public class FFmpegProbeResult {
    public FFmpegError error;
    public FFmpegFormat format;
    public List<FFmpegStream> streams;

    public FFmpegError getError() {
        return error;
    }

    public boolean hasError() {
        return error != null;
    }

    public FFmpegFormat getFormat() {
        return format;
    }

    public List<FFmpegStream> getStreams() {
        return ImmutableList.copyOf(streams);
    }
}
