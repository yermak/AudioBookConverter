package uk.yermak.audiobookconverter;

import com.freeipodsoftware.abc.Util;
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Header;
import org.apache.commons.io.IOUtils;
import org.farng.mp3.MP3File;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Map;

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

    public static MediaInfo determineChannelsAndFrequency(String filename) {
        MediaInfo mediaInfo = new MediaInfo(filename);
        FileInputStream in = null;
        try {

            File file = new File(filename);
            AudioFileFormat baseFileFormat = AudioSystem.getAudioFileFormat(file);
            Map properties = baseFileFormat.properties();
            Long duration = (Long) properties.get("duration");


            in = new FileInputStream(filename);
            BufferedInputStream sourceStream = new BufferedInputStream(in);
            Bitstream stream = new Bitstream(sourceStream);
            MP3File mp3File = new MP3File(filename);
            mp3File.getMp3StartByte();

//            mp3File.getFrequency();
//            mp3File.getBitRate();
//            mp3File.getMode();

            Header header = stream.readFrame();
            mediaInfo.setChannels(header.mode() == 3 ? 1 : 2);
            mediaInfo.setFrequency(header.frequency());
            mediaInfo.setBitrate(header.bitrate());
            int streamSize = (int) in.getChannel().size();
            mediaInfo.setDuration(Math.round(header.total_ms((int) (streamSize - mp3File.getMp3StartByte()))));
            return mediaInfo;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }
}
