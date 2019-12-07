package uk.yermak.audiobookconverter;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;

public class ArtWorkImage implements ArtWork {
    private Image image;
    private final Logger logger;
    private ArtWork bean;

    public Image image() {
        return this.image;
    }

    private Logger logger() {
        return this.logger;
    }

    private ArtWork bean() {
        return this.bean;
    }

    private ArtWork getBean() {
        if (this.bean() != null) {
            this.bean();
        }

        BufferedImage bImage = SwingFXUtils.fromFXImage(this.image(), (BufferedImage) null);
        String poster = Utils.getTmp((long) this.image().hashCode(), this.image().hashCode(), ".png");

        try {
            File posterFile = new File(poster);
            posterFile.deleteOnExit();
            ImageIO.write(bImage, "png", posterFile);
            long crc32 = Utils.checksumCRC32(posterFile);
            this.bean = new ArtWorkBean(poster, "png", crc32);
        } catch (IOException var7) {
            this.logger().error("Error during parallel conversion", var7);
            var7.printStackTrace();
        }

        return this.bean();
    }

    public long getCrc32() {
        return this.getBean().getCrc32();
    }

    public void setCrc32(final long crc32) {
        this.getBean().setCrc32(crc32);
    }

    public String getFileName() {
        return this.getBean().getFileName();
    }

    public void setFileName(final String fileName) {
        this.getBean().setFileName(fileName);
    }

    public String getFormat() {
        return this.getBean().getFormat();
    }

    public void setFormat(final String format) {
        this.getBean().setFormat(format);
    }

    @Override
    public boolean matchCrc32(long crc32) {
        return getCrc32() == crc32;
    }

    public ArtWorkImage(final Image image) {
        this.image = image;
        this.logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
        this.bean = null;
    }
}

        