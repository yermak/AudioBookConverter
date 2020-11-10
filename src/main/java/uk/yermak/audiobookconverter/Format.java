package uk.yermak.audiobookconverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum Format {
    M4B("ipod", "aac", "m4b") {
        @Override
        public boolean mp4Compatible() {
            return true;
        }

        @Override
        public boolean ffmpegCompatible() {
            return false;
        }
    },
    M4A("ipod", "aac", "m4a") {
        @Override
        public boolean mp4Compatible() {
            return true;
        }

        @Override
        public boolean ffmpegCompatible() {
            return false;
        }
    },
    MP3("mp3", "libmp3lame", "mp3") {
        @Override
        public List<String> getConcatOptions(String fileListFileName, MetadataBuilder metadataBuilder, String progressUri, String outputFileName) {
            List<String> options = new ArrayList<>();
            options.add(Utils.FFMPEG);
            options.add("-protocol_whitelist");
            options.add("file,pipe,concat");
            options.add("-f");
            options.add("concat");
            options.add("-safe");
            options.add("0");
            options.add("-i");
            options.add(fileListFileName);
            String artWorkFile = metadataBuilder.getArtWorkFile();
            if (artWorkFile != null) {
                options.add("-i");
                options.add(artWorkFile);
                options.add("-c:v");
                options.add("copy");
                options.add("-map");
                options.add("0:0");
                options.add("-map");
                options.add("1:0");
            } else {
                options.add("-vn");
            }
            options.add("-c:a");
            options.add("copy");
            options.add("-id3v2_version");
            options.add("4");
            options.addAll(metadataBuilder.prepareId3v2Meta());
            options.add("-f");
            options.add(format);
            options.add("-progress");
            options.add(progressUri);
            options.add(outputFileName);

            return options;
        }
    },
    OGG("ogg", "libopus", "ogg") {
        @Override
        public List<String> getConcatOptions(String fileListFileName, MetadataBuilder metadataBuilder, String progressUri, String outputFileName) {
            List<String> options = new ArrayList<>();
            options.add(Utils.FFMPEG);
            options.add("-protocol_whitelist");
            options.add("file,pipe,concat");
            options.add("-f");
            options.add("concat");
            options.add("-safe");
            options.add("0");
            options.add("-i");
            options.add(fileListFileName);
            options.add("-i");
            options.add(metadataBuilder.prepareOggMetaFile().getAbsolutePath());
            options.add("-map_metadata");
            options.add("1");
            options.add("-c:a");
            options.add("copy");
            options.add("-vn");
            options.add("-f");
            options.add(format);
            options.add("-progress");
            options.add(progressUri);
            options.add(outputFileName);

            return options;

        }

        @Override
        public List<String> getReencodingOptions(MediaInfo mediaInfo, String progressUri, String outputFileName, OutputParameters outputParameters) {
            List<String> options = new ArrayList<>();
            options.add(Utils.FFMPEG);
            if (mediaInfo.getOffset() != -1) {
                options.add("-ss");
                options.add(toFFMpegTime(mediaInfo.getOffset()));
            }
            options.add("-i");
            options.add(mediaInfo.getFileName());
            options.add("-vn");
            options.add("-codec:a");
            options.add(codec);
            options.add("-f");
            options.add(format);
            options.add(outputParameters.getFFMpegQualityParameter());
            options.add(outputParameters.getFFMpegQualityValue());
            options.add("-ac");
            options.add(String.valueOf(outputParameters.getFFMpegChannelsValue()));
            if (mediaInfo.getOffset() != -1) {
                options.add("-t");
                options.add(toFFMpegTime(mediaInfo.getDuration()));
            }
            options.add("-cutoff");
            options.add(outputParameters.getCutoffValue());
            options.add("-progress");
            options.add(progressUri);
            options.add(outputFileName);
            return options;
        }
    };

    protected String format;
    protected String codec;
    protected String extension;

    Format(String format, String codec, String extension) {
        this.format = format;
        this.codec = codec;
        this.extension = extension;
    }

    static String toFFMpegTime(long time) {
        return (time / 1000) + "." + time % 1000;
    }

    public static Format instance(String extension) {
        String ext = extension.toLowerCase();
        return switch (ext) {
            case "m4b" -> Format.M4B;
            case "m4a" -> Format.M4A;
            case "mp3" -> Format.MP3;
            case "ogg" -> Format.OGG;
            default -> Format.M4B;
        };
    }

    public List<String> getReencodingOptions(MediaInfo mediaInfo, String progressUri, String outputFileName, OutputParameters outputParameters) {
        List<String> options = new ArrayList<>();
        options.add(Utils.FFMPEG);
        if (mediaInfo.getOffset() != -1) {
            options.add("-ss");
            options.add(toFFMpegTime(mediaInfo.getOffset()));
        }
        options.add("-i");
        options.add(mediaInfo.getFileName());
        options.add("-vn");
        options.add("-codec:a");
        options.add(codec);
        options.add("-f");
        options.add(format);
        options.add(outputParameters.getFFMpegQualityParameter());
        options.add(outputParameters.getFFMpegQualityValue());
        options.add("-ac");
        options.add(String.valueOf(outputParameters.getFFMpegChannelsValue()));
        if (mediaInfo.getOffset() != -1) {
            options.add("-t");
            options.add(toFFMpegTime(mediaInfo.getDuration()));
        }
        options.add("-cutoff");
        options.add(outputParameters.getCutoffValue());
        options.add("-progress");
        options.add(progressUri);
        options.add(outputFileName);
        return options;
    }

    public List<String> getTranscodingOptions(MediaInfo mediaInfo, String progressUri, String outputFileName) {
        List<String> options = new ArrayList<>();
        options.add(Utils.FFMPEG);
        if (mediaInfo.getOffset() != -1) {
            options.add("-ss");
            options.add(toFFMpegTime(mediaInfo.getOffset()));
        }
        options.add("-i");
        options.add(mediaInfo.getFileName());
        options.add("-map_metadata");
        options.add("-1");
        options.add("-map_chapters");
        options.add("-1");
        options.add("-vn");
        options.add("-codec:a");
        options.add("copy");
        if (mediaInfo.getOffset() != -1) {
            options.add("-t");
            options.add(toFFMpegTime(mediaInfo.getDuration()));
        }
        options.add("-f");
        options.add(format);
        options.add("-progress");
        options.add(progressUri);
        options.add(outputFileName);
        return options;
    }

    public List<String> getConcatOptions(String fileListFileName, MetadataBuilder metadataBuilder, String progressUri, String outputFileName) {
        String[] strings = {Utils.FFMPEG,
                "-protocol_whitelist", "file,pipe,concat",
                "-vn",
                "-f", "concat",
                "-safe", "0",
                "-i", fileListFileName,
                "-i", metadataBuilder.prepareMp4MetaFile().getAbsolutePath(),
                "-map_metadata", "1",
                "-f", format,
                "-c:a", "copy",
                "-movflags", "+faststart",
                "-progress", progressUri,
                outputFileName};
        return Arrays.asList(strings);
    }


    public boolean needsReencode(String codec) {
        return !this.codec.equalsIgnoreCase(codec);
    }

    @Override
    public String toString() {
        return extension;
    }

    public boolean mp4Compatible() {
        return false;
    }

    public boolean ffmpegCompatible() {
        return true;
    }
}
