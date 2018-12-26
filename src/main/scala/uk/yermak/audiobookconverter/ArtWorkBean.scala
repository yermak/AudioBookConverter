package uk.yermak.audiobookconverter

/**
  * Created by yermak on 1/11/2018.
  */
class ArtWorkBean(var fileName: String, var crc32: Long) extends ArtWork {
  override def getCrc32: Long = crc32

  def setCrc32(crc32: Long): Unit = this.crc32 = crc32

  override def getFileName: String = fileName

  override def setFileName(fileName: String): Unit = this.fileName = fileName
}