package uk.yermak.audiobookconverter

/**
  * Created by Yermak on 02-Jan-18.
  */
class MediaInfoBean(var fileName: String) extends MediaInfo {
  private var channels = 2
  private var frequency = 44100
  private var bitrate = 128000
  private var duration = 0L
  private var bookInfo: AudioBookInfo = null
  private var artWork: ArtWork = null
  private var codec: String = "";

  override def setChannels(channels: Int): Unit = this.channels = channels

  override def setFrequency(frequency: Int): Unit = this.frequency = frequency

  override def setBitrate(bitrate: Int): Unit = this.bitrate = bitrate

  override def setDuration(duration: Long): Unit = this.duration = duration

  override def getChannels: Int = channels

  override def getFrequency: Int = frequency

  override def getBitrate: Int = bitrate

  override def getDuration: Long = duration

  override def getFileName: String = fileName

  override def setBookInfo(bookInfo: AudioBookInfo): Unit = this.bookInfo = bookInfo

  override def getBookInfo: AudioBookInfo = bookInfo

  override def getArtWork: ArtWork = artWork

  override def setArtWork(artWork: ArtWork): Unit = this.artWork = artWork

  override def getCodec: String = codec

  override def setCodec(codec: String): Unit = this.codec = codec
}