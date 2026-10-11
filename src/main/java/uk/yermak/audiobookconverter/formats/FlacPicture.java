package uk.yermak.audiobookconverter.formats;

import javafx.scene.image.Image;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.yermak.audiobookconverter.ConversionException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

public record FlacPicture(int type, String description, String mime, int width, int height, int depth, byte[] data) {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static FlacPicture load(String artWorkFile) {
        try {
            // read once and decode from memory: an unclosed file stream kept the poster locked on Windows
            byte[] data = Files.readAllBytes(Path.of(artWorkFile));
            Image image = new Image(new ByteArrayInputStream(data));
            String extension = FilenameUtils.getExtension(artWorkFile).toLowerCase();
            String mime = switch (extension) {
                case "png" -> "image/png";
                case "jpg", "jfif", "jpeg" -> "image/jpeg";
                default -> {
                    logger.warn("Image format {} is not supported for OGG files", extension);
                    yield "image/" + extension; // not null: write() needs a MIME string
                }
            };
            return new FlacPicture(3, "Cover", mime, ((int) image.getWidth()), ((int) image.getHeight()), 24, data);
        } catch (IOException e) {
            logger.error("Failed to create FlacPicture from file", e);
            throw new ConversionException("Failed to create FlacPicture from file", e);
        }
    }

    public String write() {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            out.write(ByteBuffer.allocate(4).putInt(type).array());
            byte[] mimeBytes = mime.getBytes();
            out.write(ByteBuffer.allocate(4).putInt(mimeBytes.length).array());
            out.write(mimeBytes);
            byte[] descBytes = description.getBytes();
            out.write(ByteBuffer.allocate(4).putInt(descBytes.length).array());
            out.write(descBytes);

            out.write(ByteBuffer.allocate(4).putInt(width).array());
            out.write(ByteBuffer.allocate(4).putInt(height).array());
            out.write(ByteBuffer.allocate(4).putInt(depth).array());
            int numberOfColors = 0;
            out.write(ByteBuffer.allocate(4).putInt(numberOfColors).array());
            out.write(ByteBuffer.allocate(4).putInt(data.length).array());
            out.write(data);

            return Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (IOException e) {
            throw new ConversionException(e);
        }
    }
}
