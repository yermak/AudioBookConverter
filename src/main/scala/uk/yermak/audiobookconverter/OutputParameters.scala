package uk.yermak.audiobookconverter

import java.util

import scala.collection.JavaConverters

class OutputParameters(var bitRate: Int = 128,
                       var frequency: Int = 44100,
                       var channels: Int = 2,
                       var quality: Int = 3,
                       var cbr: Boolean = true,
                       var auto: Boolean = true,
                       var parts: Int = 1,
                       var cutoff: Int = 10000,
                      ) {

  val cuttoffs = Map(1 -> "13050", 2 -> "13050", 3 -> "14260", 4 -> "15500")

  def getBitRate: Int = bitRate

  def setBitRate(bitRate: Int) = this.bitRate = bitRate

  def getFrequency: Int = frequency

  def setFrequency(frequency: Int) = this.frequency = frequency

  def getChannels: Int = channels

  def setChannels(channels: Int) = this.channels = channels

  def getQuality: Int = quality

  def setQuality(quality: Int) = this.quality = quality

  def isCbr: Boolean = cbr

  def setCbr(cbr: Boolean) = this.cbr = cbr

  def isAuto: Boolean = auto

  def setAuto(auto: Boolean) = this.auto = auto

  def getParts: Int = parts

  def setParts(parts: Int) = this.parts = parts

  def getCutoff: Int = cutoff

  def setCutoff(cutOff: Int) = this.cutoff = cutOff

  def updateAuto(mediaList: util.List[MediaInfo]): Unit = {
    if (auto) {
      val media = JavaConverters.iterableAsScalaIterable(mediaList)
      channels = media.maxBy(mediaInfo => mediaInfo.getChannels).getChannels
      frequency = media.maxBy(mediaInfo => mediaInfo.getFrequency).getFrequency
      if (cbr) bitRate = media.maxBy(mediaInfo => mediaInfo.getBitrate).getBitrate
    }
  }

  def getFFMpegQualityParameter: String = if (cbr) "-b:a" else "-vbr"

  def getFFMpegQualityValue: String = if (cbr) getBitRate + "k" else quality.toString

  def getFFMpegFrequencyValue: String = getFrequency.toString

  def getFFMpegChannelsValue: String = getChannels.toString

  def getCutoffValue: String = {
    if (cbr) cutoff.toString
    else cuttoffs.getOrElse(quality, "0")
  }
}

object OutputParameters {
  def instance() = new OutputParameters()
}
