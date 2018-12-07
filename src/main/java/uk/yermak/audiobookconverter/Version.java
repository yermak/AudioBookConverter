package uk.yermak.audiobookconverter;

public class Version {
    public static final int MAJOR = 3;
    public static final int MINOR = 0;
    public static final int BUILD = 0;

    public Version() {
    }

    public static String getVersionString() {
        //TODO add load version from the build number.
        return "AudioBookConverter " + MAJOR + "." + MINOR + (BUILD != 0 ? "." + BUILD : "");
    }
}
