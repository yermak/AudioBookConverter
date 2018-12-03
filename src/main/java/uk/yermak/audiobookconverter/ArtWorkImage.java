package uk.yermak.audiobookconverter;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created by yermak on 16-Nov-18.
 */
public class ArtWorkImage implements ArtWork {

    private Image image;
    private ArtWorkBean bean;

    public ArtWorkImage(Image image) {
        this.image = image;
    }

    private ArtWorkBean getBean() {
        if (bean != null) return bean;
        BufferedImage bImage = SwingFXUtils.fromFXImage(image, null);
        String poster = Utils.getTmp(image.hashCode(), image.hashCode(), "." + "png");
        try {
            File posterFile = new File(poster);
            posterFile.deleteOnExit();
            ImageIO.write(bImage, "png", posterFile);
            long crc32 = Utils.checksumCRC32(posterFile);
            bean = new ArtWorkBean(poster, crc32);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bean;
    }

    @Override
    public Long getCrc32() {
        return getBean().getCrc32();
    }

    @Override
    public void setCrc32(Long crc32) {
        getBean().setCrc32(crc32);
    }

    @Override
    public String getFileName() {
        return getBean().getFileName();
    }

    @Override
    public void setFileName(String fileName) {
        getBean().setFileName(fileName);
    }

}
