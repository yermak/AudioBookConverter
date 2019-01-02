package uk.yermak.audiobookconverter

import java.util.concurrent.{ExecutionException, Future}

/**
  * Created by Yermak on 03-Jan-18.
  */
class MediaInfoProxy(val filename: String, val futureLoad: Future[MediaInfo]) extends MediaInfo {
  private def getMediaInfo = try futureLoad.get
  catch {
    case e@(_: InterruptedException | _: ExecutionException) =>
      e.printStackTrace()
      throw new RuntimeException(e)
  }

  override def setChannels(channels: Int): Unit = getMediaInfo.setChannels(channels)

  override def setFrequency(frequency: Int): Unit = getMediaInfo.setFrequency(frequency)

  override def setBitrate(bitrate: Int): Unit = getMediaInfo.setBitrate(bitrate)

  override def setDuration(duration: Long): Unit = getMediaInfo.setDuration(duration)

  override def getChannels: Int = getMediaInfo.getChannels

  override def getFrequency: Int = getMediaInfo.getFrequency

  override def getBitrate: Int = getMediaInfo.getBitrate

  override def getDuration: Long = getMediaInfo.getDuration

  override def getFileName: String = filename

  override def setBookInfo(bookInfo: AudioBookInfo): Unit = getMediaInfo.setBookInfo(bookInfo)

  override def getBookInfo: AudioBookInfo = getMediaInfo.getBookInfo

  override def getArtWork: ArtWork = getMediaInfo.getArtWork

  override def setArtWork(artWork: ArtWork): Unit = getMediaInfo.setArtWork(artWork)

  override def toString: String = filename

  override def getCodec: String = getMediaInfo.getCodec

  override def setCodec(codec: String): Unit = getMediaInfo.setCodec(codec)
}