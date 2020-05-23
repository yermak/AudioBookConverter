package uk.yermak.audiobookconverter;

import java.util.Collections;
import java.util.List;

public interface Convertable {
    List<MediaInfo> getMedia();

    List<String> getMetaData(AudioBookInfo bookInfo);

    int getNumber();

    int getTotalNumbers();

    boolean isTheOnlyOne();

    long getDuration();

    Convertable EMPTY = new EmptyConvertable();

    String getDetails();

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
        public int getTotalNumbers() {
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

        @Override
        public String getDetails() {
            return "";
        }
    }
}
