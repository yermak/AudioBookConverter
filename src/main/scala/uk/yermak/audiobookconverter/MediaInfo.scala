package uk.yermak.audiobookconverter

/**
  * Created by Yermak on 03-Jan-18.
  */
trait MediaInfo {
  def setChannels(channels: Int): Unit

  def setFrequency(frequency: Int): Unit

  def setBitrate(bitrate: Int): Unit

  def setDuration(duration: Long): Unit

  def getChannels: Int

  def getFrequency: Int

  def getBitrate: Int

  def getDuration: Long

  def getFileName: String

  def setBookInfo(bookInfo: AudioBookInfo): Unit

  def getBookInfo: AudioBookInfo

  def getArtWork: ArtWork

  def setArtWork(artWork: ArtWork): Unit
}
