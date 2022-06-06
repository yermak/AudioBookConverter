package uk.yermak.audiobookconverter;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.lang3.StringUtils;
import uk.yermak.audiobookconverter.fx.util.SmartIntegerProperty;
import uk.yermak.audiobookconverter.fx.util.SmartStringProperty;

import java.util.Collections;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.firstNonBlank;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;


public record AudioBookInfo(SmartStringProperty title, SmartStringProperty writer,
                            SmartStringProperty narrator, SmartStringProperty series,
                            SmartStringProperty genre, SmartStringProperty year,
                            SmartIntegerProperty bookNumber, SmartIntegerProperty totalTracks,
                            SmartStringProperty comment,
                            ObservableList<Track> tracks) {


    public static AudioBookInfo instance(Map<String, String> tags) {
        int trackNumber = tags.get("track number") == null ? 0 : Integer.parseInt(tags.get("track number"));
        int trackCount = tags.get("track count") == null ? 0 : Integer.parseInt(tags.get("track count"));

        String track = tags.get("track");
        if (StringUtils.isNotEmpty(track)) {
            String[] split = StringUtils.split(track, "/");
            trackNumber = Integer.parseInt(split[0]);
            if (split.length > 1) {
                trackCount = Integer.parseInt(split[1]);
            }
        }

        return new AudioBookInfo(
                new SmartStringProperty(trimToEmpty(tags.get("title"))),
                new SmartStringProperty(trimToEmpty(tags.get("artist"))),
                new SmartStringProperty(firstNonBlank(trimToEmpty(tags.get("narratedby")), trimToEmpty(tags.get("composer")))),
                new SmartStringProperty(trimToEmpty(tags.get("album"))),
                new SmartStringProperty(trimToEmpty(tags.get("genre"))),
                new SmartStringProperty(firstNonBlank(trimToEmpty(tags.get("year")), StringUtils.substring(trimToEmpty(tags.get("date")), 0, 4))),
                new SmartIntegerProperty(trackNumber),
                new SmartIntegerProperty(trackCount),
                new SmartStringProperty(firstNonBlank(trimToEmpty(tags.get("comment")), trimToEmpty(tags.get("comment-0")))),
                FXCollections.observableArrayList()
        );
    }

    public static AudioBookInfo instance() {
        return instance(Collections.emptyMap());
    }
}

        