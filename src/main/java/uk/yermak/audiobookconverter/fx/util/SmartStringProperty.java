package uk.yermak.audiobookconverter.fx.util;

import javafx.beans.property.SimpleStringProperty;
import org.apache.commons.lang3.StringUtils;

public class SmartStringProperty extends SimpleStringProperty {


    public SmartStringProperty() {
    }

    public SmartStringProperty(String s) {
        super(s);
    }

    public SmartStringProperty(Object o, String s) {
        super(o, s);
    }

    public SmartStringProperty(Object o, String s, String s1) {
        super(o, s, s1);
    }

    @Override
    public String toString() {
        return getValueSafe();
    }

    public boolean isBlank() {
        return StringUtils.isBlank(getValueSafe());
    }

    public boolean isNotBlank() {
        return !isBlank();
    }

    public String trimToNull() {
        return StringUtils.trimToNull(getValue());
    }
}
