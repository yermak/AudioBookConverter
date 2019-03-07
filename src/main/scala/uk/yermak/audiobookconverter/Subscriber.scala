package uk.yermak.audiobookconverter

import uk.yermak.audiobookconverter.fx.ConversionProgress

/**
  * Created by yermak on 3/7/2019.
  */
trait Subscriber {
  def addConversionProgress(conversionProgress: ConversionProgress)
}
