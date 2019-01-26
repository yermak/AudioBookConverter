package uk.yermak.audiobookconverter

/**
  * Created by Yermak on 29-Dec-17.
  */
class ConverterOutput(var mediaInfo: MediaInfo, val outputFileName: String) {
  def getOutputFileName: String = outputFileName

  def getDuration: Long = mediaInfo.getDuration

  def getMediaInfo: MediaInfo = mediaInfo
}
