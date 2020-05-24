package uk.yermak.audiobookconverter;

import javafx.beans.property.SimpleIntegerProperty;

public class SmartIntegerProperty extends SimpleIntegerProperty {
    public SmartIntegerProperty() {
    }

    public SmartIntegerProperty(int i) {
        super(i);
    }

    public SmartIntegerProperty(Object o, String s) {
        super(o, s);
    }

    public SmartIntegerProperty(Object o, String s, int i) {
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

    public Integer zeroToNull() {
        if (get() == 0) return null;
        return getValue();
    }
}
