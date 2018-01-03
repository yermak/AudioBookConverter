package uk.yermak.audiobookconverter;

import com.freeipodsoftware.abc.Mp4Tags;
import com.freeipodsoftware.abc.Util;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.probe.FFmpegFormat;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.probe.FFmpegStream;
import org.apache.commons.lang3.StringUtils;
import org.farng.mp3.MP3File;
import org.farng.mp3.id3.AbstractID3v2;
import org.farng.mp3.id3.ID3v1;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by Yermak on 29-Dec-17.
 */
public class Utils {
    static String determineTempFilename(String inputFilename, final String extension, String prefix, final String suffix, boolean uniqie, String folder) {
        File file = new File(inputFilename);
        File outFile = new File(folder, prefix + file.getName());
        String result = outFile.getAbsolutePath().replaceAll("(?i)\\." + extension, "." + suffix);
        if (!result.endsWith("." + suffix)) {
            result = result + "." + suffix;
        }
        if (uniqie) {
            return Util.makeFilenameUnique(result);
        }
        return result;
    }

    public static MediaInfo loadMediaInfo(String filename) {
        try {

            Future futureLoad = Executors.newWorkStealingPool().submit(new MediaInfoCallable(filename));
            MediaInfo mediaInfo = new MediaInfoProxy(filename, futureLoad);

            return mediaInfo;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
        }
    }

    public static Mp4Tags readTagsFromInputFile(String filename) {
        Mp4Tags tags = new Mp4Tags();

        try {
            MP3File file = new MP3File(new File(filename));
            AbstractID3v2 v2Tag = file.getID3v2Tag();
            if (v2Tag != null) {
                importV2Tags(tags, v2Tag);
            } else {
                ID3v1 v1Tag = file.getID3v1Tag();
                if (v1Tag != null) {
                    importV1Tags(tags, v1Tag);
                }
            }
        } catch (Exception var5) {
        }

        return tags;
    }

    private static void importV1Tags(Mp4Tags tags, ID3v1 v1Tag) {
        tags.setWriter(filterTag(v1Tag.getArtist()));
        tags.setTitle(filterTag(v1Tag.getTitle()));
        tags.setSeries(filterTag(v1Tag.getAlbum()));
        tags.setGenre(filterTag(v1Tag.getSongGenre()));
        tags.setYear(filterTag(v1Tag.getYear()));
        tags.setComment(filterTag(v1Tag.getComment()));
    }

    private static void importV2Tags(Mp4Tags tags, AbstractID3v2 v2Tag) {
        tags.setWriter(filterTag(v2Tag.getLeadArtist()));
        tags.setTitle(filterTag(v2Tag.getSongTitle()));
        tags.setSeries(filterTag(v2Tag.getAlbumTitle()));
        tags.setGenre(filterTag(v2Tag.getSongGenre()));
        tags.setYear(String.valueOf(v2Tag.getYearReleased()));

/*
        if (StringUtils.isNotBlank(v2Tag.getTrackNumberOnAlbum()) && StringUtils.isNotBlank(v2Tag.getTrackNumberOnAlbum())) {
            tags.setTrack(v2Tag.getTrackNumberOnAlbum() + "/" + v2Tag.getTrackNumberOnAlbum());

        } else
*/
        if (StringUtils.isNotBlank(v2Tag.getTrackNumberOnAlbum()) && StringUtils.isNumeric(v2Tag.getTrackNumberOnAlbum())) {
            tags.setTrack(Integer.valueOf(v2Tag.getTrackNumberOnAlbum()));
        }

        tags.setComment(filterTag(v2Tag.getSongComment()));
    }

    private static String filterTag(String tag) {
        return tag == null ? "" : tag.trim();
    }

    private static class MediaInfoCallable implements Callable<MediaInfo> {

        private final String filename;

        public MediaInfoCallable(String filename) {
            this.filename = filename;
        }

        @Override
        public MediaInfo call() throws Exception {
            MediaInfoBean mediaInfo = new MediaInfoBean(filename);
            FFprobe ffprobe = new FFprobe("external/x64/ffprobe.exe");
            FFmpegProbeResult probeResult = ffprobe.probe(filename);
            FFmpegFormat format = probeResult.getFormat();

            List<FFmpegStream> streams = probeResult.getStreams();
            for (int i = 0; i < streams.size(); i++) {
                FFmpegStream fFmpegStream = streams.get(i);
                if ("mp3".equals(fFmpegStream.codec_name)) {
                    mediaInfo.setChannels(fFmpegStream.channels);
                    mediaInfo.setFrequency(fFmpegStream.sample_rate);
                    mediaInfo.setBitrate((int) fFmpegStream.bit_rate);
                    mediaInfo.setDuration((long) fFmpegStream.duration * 1000);
                    break;
                }
            }
            Mp4Tags mp4Tags = new Mp4Tags(format.tags);
            mediaInfo.setMp4Tags(mp4Tags);
            return mediaInfo;
        }
    }
}
