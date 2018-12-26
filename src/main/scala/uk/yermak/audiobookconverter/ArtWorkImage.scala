package uk.yermak.audiobookconverter

import java.io.{File, IOException}

import javafx.embed.swing.SwingFXUtils
import javafx.scene.image.Image
import javax.imageio.ImageIO

/**
  * Created by yermak on 16-Nov-18.
  */
class ArtWorkImage(var image: Image) extends ArtWork {
  private var bean: ArtWork = null

  private def getBean: ArtWork = {
    if (bean != null) bean
    val bImage = SwingFXUtils.fromFXImage(image, null)
    val poster = Utils.getTmp(image.hashCode, image.hashCode, "." + "png")
    try {
      val posterFile = new File(poster)
      posterFile.deleteOnExit()
      ImageIO.write(bImage, "png", posterFile)
      val crc32 = Utils.checksumCRC32(posterFile)
      bean = new ArtWorkBean(poster, crc32)
    } catch {
      case e: IOException =>
        e.printStackTrace()
    }
    bean
  }

  override def getCrc32: Long = getBean.getCrc32

  def setCrc32(crc32: Long): Unit = getBean.setCrc32(crc32)

  override def getFileName: String = getBean.getFileName

  override def setFileName(fileName: String): Unit =
    getBean.setFileName(fileName)
}
