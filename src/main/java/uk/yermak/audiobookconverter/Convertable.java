package uk.yermak.audiobookconverter;

import java.util.List;

public interface Convertable {
    List<MediaInfo> getMedia();

    List<String> getMetaData(AudioBookInfo bookInfo);
}
