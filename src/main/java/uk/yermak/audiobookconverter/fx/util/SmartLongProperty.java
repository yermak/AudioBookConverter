package uk.yermak.audiobookconverter.fx.util;

import javafx.beans.property.SimpleLongProperty;

public class SmartLongProperty extends SimpleLongProperty {
    public SmartLongProperty() {
    }

    public SmartLongProperty(long i) {
        super(i);
    }

    public SmartLongProperty(Object o, String s) {
        super(o, s);
    }

    public SmartLongProperty(Object o, String s, int i) {
        super(o, s, i);
    }

    @Override
    public String toString() {
        if (getValue() == null) return "";
        return String.valueOf(getValue());
    }

    public void set(String value) {
        set(Integer.parseInt(value));
    }

    public Long zeroToNull() {
        if (get() == 0) return null;
        return getValue();
    }
}
