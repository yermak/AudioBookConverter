package uk.yermak.audiobookconverter;

import java.util.Collections;
import java.util.List;

public interface Convertable {
    List<MediaInfo> getMedia();

    List<String> getMetaData(AudioBookInfo bookInfo);

    int getNumber();

    boolean isTheOnlyOne();

    long getDuration();

    public static final Convertable EMPTY = new EmptyConvertable();

    class EmptyConvertable implements Convertable {
        @Override
        public List<MediaInfo> getMedia() {
            return Collections.emptyList();
        }

        @Override
        public List<String> getMetaData(AudioBookInfo bookInfo) {
            return Collections.emptyList();
        }

        @Override
        public int getNumber() {
            return 0;
        }

        @Override
        public boolean isTheOnlyOne() {
            return true;
        }

        @Override
        public long getDuration() {
            return 0;
        }
    }
}
