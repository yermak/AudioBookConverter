package uk.yermak.audiobookconverter

/**
  * Created by Yermak on 03-Jan-18.
  */
class ProgressCallback(var fileName: String, var refreshable: Refreshable) {
  def converted(timeInMillis: Long, size: Long): Unit = refreshable.converted(fileName, timeInMillis, size)

  def completedConversion(): Unit = refreshable.incCompleted(fileName)

  def reset(): Unit = refreshable.reset()
}