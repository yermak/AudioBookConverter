package uk.yermak.audiobookconverter;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Collections;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.firstNonBlank;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;


public record AudioBookInfo(SmartStringProperty title, SmartStringProperty writer,
                            SmartStringProperty narrator, SmartStringProperty series,
                            SmartStringProperty genre, SmartStringProperty year,
                            SmartIntegerProperty bookNumber, SmartIntegerProperty totalTracks,
                            SmartStringProperty comment,
                            ObservableList<Track>tracks) {


    public static AudioBookInfo instance(Map<String, String> tags) {
        String trackNumber = tags.get("track number");
        String trackCount = tags.get("track count");

        return new AudioBookInfo(
                new SmartStringProperty(trimToEmpty(tags.get("album"))),
                new SmartStringProperty(trimToEmpty(tags.get("artist"))),
                new SmartStringProperty(firstNonBlank(trimToEmpty(tags.get("narratedby")), trimToEmpty(tags.get("composer")))),
                new SmartStringProperty(trimToEmpty(tags.get("album"))),
                new SmartStringProperty(trimToEmpty(tags.get("genre"))),
                new SmartStringProperty(trimToEmpty(tags.get("year"))),
                new SmartIntegerProperty(trackNumber == null ? 0 : Integer.parseInt(trackNumber)),
                new SmartIntegerProperty(trackCount == null ? 0 : Integer.parseInt(trackCount)),
                new SmartStringProperty(firstNonBlank(trimToEmpty(tags.get("comment")), trimToEmpty(tags.get("comment-0")))),
                FXCollections.observableArrayList()
        );
    }

    public static AudioBookInfo instance() {
        return instance(Collections.emptyMap());
    }
}

        