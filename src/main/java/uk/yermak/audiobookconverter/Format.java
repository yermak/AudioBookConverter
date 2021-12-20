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
        public List<String> getReencodingOptions(MediaInfo mediaInfo, String progressUri, String outputFileName, OutputParameters outputParameters) {
            List<String> options = new ArrayList<>();
            options.add(Environment.FFMPEG);
            if (mediaInfo.getOffset() != -1) {
                options.add("-ss");
                options.add(toFFMpegTime(mediaInfo.getOffset()));
            }
            options.add("-i");
            options.add(mediaInfo.getFileName());
            options.add("-vn");
            options.add("-codec:a");
            options.add(codec);

            options.add("-map_metadata");
            options.add( "-1");

            options.addAll(outputParameters.cbr
                    ? List.of("-b:a", outputParameters.getBitRate() + "k")
                    : List.of("-q:a", String.valueOf(10 - outputParameters.vbrQuality * 2))
            );

            options.add("-ac");
            options.add(String.valueOf(outputParameters.getChannels()));
            if (mediaInfo.getOffset() != -1) {
                options.add("-t");
                options.add(toFFMpegTime(mediaInfo.getDuration()));
            }
            if (outputParameters.getCutoff() != null) {
                options.add("-cutoff");
                options.add(Integer.toString(outputParameters.getCutoff()));
            }
            options.add("-f");
            options.add(format);
            options.add("-progress");
            options.add(progressUri);
            options.add(outputFileName);
            return options;

        }

        @Override
        public Integer defaultBitrate() {
            return 96;
        }

        @Override
        public Integer defaultFrequency() {
            return 22050;
        }

        @Override
        public List<Integer> channels() {
            return List.of(1, 2, 6);
        }

        @Override
        public List<Integer> frequencies() {
            return List.of(8000, 11025, 16000, 22050, 24000, 32000, 44100, 48000);
        }

        @Override
        public List<Integer> bitrates() {
            return List.of(32, 48, 64, 96, 112, 128, 160, 192, 224, 256, 320);
        }

        @Override
        public Integer defaultVbrQuality() {
            return 3;
        }

        @Override
        public List<String> getConcatOptions(String fileListFileName, MetadataBuilder metadataBuilder, String progressUri, String outputFileName) {
            List<String> options = new ArrayList<>();
            options.add(Environment.FFMPEG);
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
        public List<Integer> cutoffs() {
            return List.of(4000, 6000, 8000, 12000, 20000);
        }

        @Override
        public List<Integer> frequencies() {
            return List.of(8000, 12000, 16000, 24000, 32000, 48000);
        }

        @Override
        public List<Integer> bitrates() {
            return List.of(16, 24, 32, 48, 56, 64, 96, 112, 128, 144, 160, 192, 224, 256, 320, 512);
        }

        @Override
        public Integer defaultBitrate() {
            return 64;
        }

        @Override
        public Integer defaultCutoff() {
            return 8000;
        }

        @Override
        public Integer defaultFrequency() {
            return 48000;
        }

        @Override
        public Boolean defaultCBR() {
            return false;
        }

        @Override
        public List<String> getConcatOptions(String fileListFileName, MetadataBuilder metadataBuilder, String progressUri, String outputFileName) {
            List<String> options = new ArrayList<>();
            options.add(Environment.FFMPEG);
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
            options.add(Environment.FFMPEG);
            if (mediaInfo.getOffset() != -1) {
                options.add("-ss");
                options.add(toFFMpegTime(mediaInfo.getOffset()));
            }
            options.add("-i");
            options.add(mediaInfo.getFileName());
            options.add("-vn");
            options.add("-codec:a");
            options.add(codec);

            options.add("-map_metadata");
            options.add( "-1");

            if (outputParameters.cbr) {
                options.addAll(List.of("-b:a", outputParameters.getBitRate() + "k"));
                options.addAll(List.of("-vbr", "off"));
            } else {
                options.addAll(List.of("-b:a", outputParameters.getVbrQuality() * outputParameters.getVbrQuality() * 16 + "k"));
                options.addAll(List.of("-vbr", "on"));
            }

            options.add("-ac");
            options.add(String.valueOf(outputParameters.getChannels()));
            if (mediaInfo.getOffset() != -1) {
                options.add("-t");
                options.add(toFFMpegTime(mediaInfo.getDuration()));
            }
            if (outputParameters.getCutoff() != null) {
                options.add("-cutoff");
                options.add(Integer.toString(outputParameters.getCutoff()));
            }
            options.add("-f");
            options.add(format);

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

    public List<Integer> channels() {
        return List.of(1, 2, 4, 6);
    }

    public List<Integer> vbrQualities() {
        return List.of(1, 2, 3, 4, 5);
    }

    public List<Integer> cutoffs() {
        return List.of(8000, 10000, 12000, 16000, 20000);
    }

    public List<Integer> frequencies() {
        return List.of(8000, 11025, 12000, 16000, 22050, 24000, 32000, 44100, 48000, 88200, 96000);
    }

    public List<Integer> bitrates() {
        return List.of(8, 16, 24, 32, 40, 48, 56, 64, 80, 96, 112, 128, 144, 160, 192, 224, 256, 320);
    }

    public List<Double> speeds() {
        return List.of(0.5, 0.6, 0.7, 0.8, 0.9, 1.0, 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 1.8, 1.9, 2.0);
    }

    public Integer defaultBitrate() {
        return 128;
    }

    public Double defaultSpeed() {
        return 1.0;
    }

    public Integer defaultChannel() {
        return 2;
    }

    public Integer defaultCutoff() {
        return 12000;
    }

    public Integer defaultFrequency() {
        return 44100;
    }

    public Integer defaultVbrQuality() {
        return 3;
    }

    public Boolean defaultCBR() {
        return true;
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
        options.add(Environment.FFMPEG);
        if (mediaInfo.getOffset() != -1) {
            options.add("-ss");
            options.add(toFFMpegTime(mediaInfo.getOffset()));
        }
        options.add("-i");
        options.add(mediaInfo.getFileName());
        options.add("-vn");
        options.add("-codec:a");
        options.add(codec);

        options.add("-map_metadata");
        options.add( "-1");

        options.addAll(outputParameters.cbr
                ? List.of("-b:a", outputParameters.getBitRate() + "k")
                : List.of("-q:a", String.valueOf(0.5 + outputParameters.vbrQuality * outputParameters.vbrQuality * 0.06))
        );

        options.add("-ac");
        options.add(String.valueOf(outputParameters.getChannels()));
        if (mediaInfo.getOffset() != -1) {
            options.add("-t");
            options.add(toFFMpegTime(mediaInfo.getDuration()));
        }
        if (outputParameters.getCutoff() != null) {
            options.add("-cutoff");
            options.add(Integer.toString(outputParameters.getCutoff()));
        }
        options.add("-f");
        options.add(format);

        if (outputParameters.getSpeed() != 1.0) {
            options.add("-filter:a");
            options.add("atempo=" + outputParameters.getSpeed());
        }

        options.add("-progress");
        options.add(progressUri);
        options.add(outputFileName);
        return options;
    }

    public List<String> getTranscodingOptions(MediaInfo mediaInfo, String progressUri, String outputFileName) {
        List<String> options = new ArrayList<>();
        options.add(Environment.FFMPEG);
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
        String[] strings = {Environment.FFMPEG,
                "-protocol_whitelist", "file,pipe,concat",
                "-vn",
                "-f", "concat",
                "-safe", "0",
                "-i", fileListFileName,
                "-i", metadataBuilder.prepareMp4MetaFile().getAbsolutePath(),
                "-map_metadata", "1",
                "-f", format,
                "-c:a", "copy",
//                "-movflags", "+faststart",
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
