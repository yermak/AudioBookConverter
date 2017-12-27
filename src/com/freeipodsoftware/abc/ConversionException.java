package com.freeipodsoftware.abc;

public class ConversionException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private String message;

    public ConversionException(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
