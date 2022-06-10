package uk.yermak.audiobookconverter.book;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.yermak.audiobookconverter.fx.util.SmartStringProperty;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.firstNonBlank;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;


public record AudioBookInfo(SmartStringProperty title, SmartStringProperty writer,
                            SmartStringProperty narrator, SmartStringProperty series,
                            SmartStringProperty genre, SmartStringProperty year,
                            SmartStringProperty bookNumber, SmartStringProperty totalTracks,
                            SmartStringProperty comment,
                            ObservableList<Track> tracks) {

    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());


    public static AudioBookInfo instance(Map<String, String> tags) {
        long trackNumber = 0;
        long trackCount = 0;
        try {
            trackNumber = tags.get("track number") == null ? 0 : Long.parseLong(tags.get("track number"));
            trackCount = tags.get("track count") == null ? 0 : Long.parseLong(tags.get("track count"));

            String track = tags.get("track");

            if (StringUtils.isNotBlank(track)) {
                String[] split = track.split("/");

                if (split.length > 0 && StringUtils.isNumeric(split[0])) {
                    trackNumber = Long.parseLong(split[0]);
                }
                if (split.length > 1 && StringUtils.isNumeric(split[1])) {
                    trackCount = Long.parseLong(split[1]);
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to parse track number:" + e);
        }

        return new AudioBookInfo(
                new SmartStringProperty(trimToEmpty(tags.get("title"))),
                new SmartStringProperty(trimToEmpty(tags.get("artist"))),
                new SmartStringProperty(firstNonBlank(trimToEmpty(tags.get("narratedby")), trimToEmpty(tags.get("composer")))),
                new SmartStringProperty(trimToEmpty(tags.get("album"))),
                new SmartStringProperty(trimToEmpty(tags.get("genre"))),
                new SmartStringProperty(firstNonBlank(trimToEmpty(tags.get("year")), StringUtils.substring(trimToEmpty(tags.get("date")), 0, 4))),
                new SmartStringProperty(trackNumber == 0 ? "" : String.valueOf(trackNumber)),
                new SmartStringProperty(trackCount == 0 ? "" : String.valueOf(trackCount)),
                new SmartStringProperty(firstNonBlank(trimToEmpty(tags.get("comment")), trimToEmpty(tags.get("comment-0")))),
                FXCollections.observableArrayList()
        );
    }

    public static AudioBookInfo instance() {
        return instance(Collections.emptyMap());
    }
}

        