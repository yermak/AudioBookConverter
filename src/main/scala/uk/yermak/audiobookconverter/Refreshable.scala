package uk.yermak.audiobookconverter

/**
  * Created by yermak on 16-Feb-18.
  */
trait Refreshable extends Runnable {
  def converted(fileName: String, timeInMillis: Long, size: Long): Unit

  def incCompleted(fileName: String): Unit

  def reset(): Unit
}
