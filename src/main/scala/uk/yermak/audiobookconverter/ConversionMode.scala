package uk.yermak.audiobookconverter

/**
  * Created by Yermak on 28-Dec-17.
  */
object ConversionMode extends Enumeration {
  type ConversionMode = Value
  val SINGLE, BATCH, PARALLEL = Value

  val m: Map[Value, (Conversion, Map[String, ProgressCallback]) => ConversionStrategy] = Map(
    SINGLE -> joiningConversionStrategy,
    BATCH -> batchConversionStrategy,
    PARALLEL -> parallelConversionStrategy
  )

  def joiningConversionStrategy(conversion: Conversion, progressCallbacks: Map[String, ProgressCallback]): ConversionStrategy = {
    new JoiningConversionStrategy(conversion, progressCallbacks)
  }

  def batchConversionStrategy(conversion: Conversion, progressCallbacks: Map[String, ProgressCallback]): ConversionStrategy = {
    new BatchConversionStrategy(conversion, progressCallbacks)
  }

  def parallelConversionStrategy(conversion: Conversion, progressCallbacks: Map[String, ProgressCallback]): ConversionStrategy = {
    new ParallelConversionStrategy(conversion, progressCallbacks)
  }

  def createConvertionStrategy(conversion: Conversion, progressCallbacks: Map[String, ProgressCallback]): ConversionStrategy = {
    m(Value)(conversion, progressCallbacks)
  }
}
