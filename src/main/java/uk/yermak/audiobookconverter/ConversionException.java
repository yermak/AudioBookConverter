package uk.yermak.audiobookconverter;

public class ConversionException extends RuntimeException {

    public ConversionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConversionException(Throwable cause) {
        super(cause);
    }

}
