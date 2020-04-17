package uk.yermak.audiobookconverter;

public class Version {
    private final static int MAJOR = 5;
    private final static int MINOR = 1;
    private final static int BUILD = 0;

    public static String getVersionString() {
        return "AudioBookConverter-" + MAJOR + "." + MINOR + "." + BUILD;
    }
}

        