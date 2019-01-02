package uk.yermak.audiobookconverter

/**
  * Created by yermak on 1/11/2018.
  */
trait ArtWork {
  def getCrc32: Long

  def setCrc32(crc32: Long): Unit

  def getFileName: String

  def setFileName(fileName: String): Unit

  def getFormat: String

  def setFormat(format: String): Unit

}
