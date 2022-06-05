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
                            ObservableList<Track>tracks) {


    public static AudioBookInfo instance(Map<String, String> tags) {
        String trackNumber = firstNonBlank(trimToEmpty(tags.get("track number")), trimToEmpty(tags.get("track")));
        String trackCount = tags.get("track count");

        return new AudioBookInfo(
                new SmartStringProperty(trimToEmpty(tags.get("title"))),
                new SmartStringProperty(trimToEmpty(tags.get("artist"))),
                new SmartStringProperty(firstNonBlank(trimToEmpty(tags.get("narratedby")), trimToEmpty(tags.get("composer")))),
                new SmartStringProperty(trimToEmpty(tags.get("album"))),
                new SmartStringProperty(trimToEmpty(tags.get("genre"))),
                new SmartStringProperty(firstNonBlank(trimToEmpty(tags.get("year")), StringUtils.substring(trimToEmpty(tags.get("date")),0, 4))),
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

        