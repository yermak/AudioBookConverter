package com.freeipodsoftware.abc;

public class Version {
    public static final int MAJOR = 2;
    public static final int MINOR = 0;

    public Version() {
    }

    public static String getVersionString() {
        return MAJOR + "." + MINOR;
    }
}
