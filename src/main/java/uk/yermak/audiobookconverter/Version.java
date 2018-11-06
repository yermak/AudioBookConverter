package uk.yermak.audiobookconverter;

public class Version {
    public static final int MAJOR = 4;
    public static final int MINOR = 1;
    public static final int BUILD = 0;

    public Version() {
    }

    public static String getVersionString() {
        //TODO add load version from the build number.
        return MAJOR + "." + MINOR + (BUILD != 0 ? "." + BUILD : "");
    }
}
