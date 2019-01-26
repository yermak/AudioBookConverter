package uk.yermak.audiobookconverter

import java.util

class OutputParameters {
  private var bitRate = 128
  private var frequency = 44100
  private var channels = 2
  private var quality = 3
  private var cbr = true
  private var auto = true
  private var parts = 1
  private var cutoff = 10000

  def getBitRate: Int = bitRate

  def setBitRate(bitRate: Int): Unit = this.bitRate = bitRate

  def getFrequency: Int = frequency

  def setFrequency(frequency: Int): Unit = this.frequency = frequency

  def getChannels: Int = channels

  def setChannels(channels: Int): Unit = this.channels = channels

  def getQuality: Int = quality

  def setQuality(quality: Int): Unit = this.quality = quality

  def isCbr: Boolean = cbr

  def setCbr(cbr: Boolean): Unit = this.cbr = cbr

  def isAuto: Boolean = auto

  def setAuto(auto: Boolean): Unit = this.auto = auto

  def getParts: Int = parts

  def setParts(parts: Int): Unit = this.parts = parts

  def updateAuto(media: util.List[MediaInfo]): Unit = {
    if (!auto) return
    var maxChannels = 0
    var maxFrequency = 0
    var maxBitrate = 0
    import scala.collection.JavaConversions._
    for (mediaInfo <- media) {
      if (mediaInfo.getChannels > maxChannels)
        maxChannels = mediaInfo.getChannels
      if (mediaInfo.getFrequency > maxFrequency)
        maxFrequency = mediaInfo.getFrequency
      if (mediaInfo.getBitrate > maxBitrate) maxBitrate = mediaInfo.getBitrate
    }
    setChannels(maxChannels)
    setFrequency(maxFrequency)
    if (cbr) setBitRate(maxBitrate / 1000)
  }

  def getFFMpegQualityParameter: String =
    if (cbr) "-b:a"
    else "-vbr"

  def getFFMpegQualityValue: String =
    if (cbr) getBitRate + "k"
    else String.valueOf(quality)

  def getFFMpegFrequencyValue: String = String.valueOf(getFrequency)

  def getFFMpegChannelsValue: String = String.valueOf(getChannels)

  def getCutoffValue: String = {
    if (cbr) return String.valueOf(cutoff)
    quality match {
      case 1 =>
        "13050"
      case 2 =>
        "13050"
      case 3 =>
        "14260"
      case 4 =>
        "15500"
      case _ =>
        "0"
    }
  }

  def setCutoff(cutoff: Int): Unit = this.cutoff = cutoff

  def getCutoff: Int = cutoff
}
