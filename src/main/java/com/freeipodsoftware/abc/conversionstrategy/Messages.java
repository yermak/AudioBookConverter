package com.freeipodsoftware.abc.conversionstrategy;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Messages {
    private static final String BUNDLE_NAME = "com.freeipodsoftware.abc.conversionstrategy.messages";
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("com.freeipodsoftware.abc.conversionstrategy.messages");

    private Messages() {
    }

    public static String getString(String key) {
        try {
            return RESOURCE_BUNDLE.getString(key);
        } catch (MissingResourceException var2) {
            return '!' + key + '!';
        }
    }
}
