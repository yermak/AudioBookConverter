package com.freeipodsoftware.abc;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;
import org.farng.mp3.MP3File;
import org.farng.mp3.id3.AbstractID3v2;
import org.farng.mp3.id3.ID3v1;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
import org.blinkenlights.jid3.ID3Exception;
import org.blinkenlights.jid3.MP3File;
import org.blinkenlights.jid3.v1.ID3V1Tag;
import org.blinkenlights.jid3.v2.ID3V2Tag;
*/

public class Util {

    public static String nullToEmptyString(String string) {
        return string == null ? "" : string;
    }

    public static boolean hasText(String text) {
        return text != null && text.trim().length() > 0;
    }

    public static void centerDialog(Shell parent, Shell shell) {
        Rectangle parentSize = parent.getBounds();
        Rectangle mySize = shell.getBounds();
        int locationX = (parentSize.width - mySize.width) / 2 + parentSize.x;
        int locationY = (parentSize.height - mySize.height) / 2 + parentSize.y;
        shell.setLocation(new Point(locationX, locationY));
    }

    public static String makeFilenameUnique(String filename) {
        Pattern extPattern = Pattern.compile("\\.(\\w+)$");
        Matcher extMatcher = extPattern.matcher(filename);
        if (extMatcher.find()) {
            try {
                String extension = extMatcher.group(1);

                for (File outputFile = new File(filename); outputFile.exists(); outputFile = new File(filename)) {
                    Pattern pattern = Pattern.compile("(?i)(.*)\\((\\d+)\\)\\." + extension + "$");
                    Matcher matcher = pattern.matcher(filename);
                    if (matcher.find()) {
                        filename = matcher.group(1) + "(" + (Integer.parseInt(matcher.group(2)) + 1) + ")." + extension;
                    } else {
                        filename = filename.replaceAll("." + extension + "$", "(1)." + extension);
                    }
                }

                return filename;
            } catch (Exception var7) {
                throw new RuntimeException(Messages.getString("Util.connotUseFilename") + " " + filename);
            }
        } else {
            throw new RuntimeException(Messages.getString("Util.connotUseFilename") + " " + filename + " (2)");
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
        tags.setGenre(filterTag(v2Tag.getSongTitle()));
        tags.setYear(String.valueOf(v2Tag.getYearReleased()));

/*
        if (StringUtils.isNotBlank(v2Tag.getTrackNumberOnAlbum()) && StringUtils.isNotBlank(v2Tag.getTrackNumberOnAlbum())) {
            tags.setTrack(v2Tag.getTrackNumberOnAlbum() + "/" + v2Tag.getTrackNumberOnAlbum());

        } else
*/
        if (StringUtils.isNotBlank(v2Tag.getTrackNumberOnAlbum())) {
            tags.setTrack("" + v2Tag.getTrackNumberOnAlbum());
        } else {
            tags.setTrack("");
        }

        tags.setComment(filterTag(v2Tag.getSongComment()));
    }

    private static String filterTag(String tag) {
        return tag == null ? "" : tag.trim();
    }
}
