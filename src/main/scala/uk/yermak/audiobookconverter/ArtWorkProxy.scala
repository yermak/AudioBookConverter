package uk.yermak.audiobookconverter

import java.lang.invoke.MethodHandles
import java.util.concurrent.{ExecutionException, Future}

import org.slf4j.{Logger, LoggerFactory}

/**
  * Created by yermak on 1/11/2018.
  */
class ArtWorkProxy(val futureLoad: Future[ArtWork], var format: String) extends ArtWork {
  private val logger: Logger = LoggerFactory.getLogger(MethodHandles.lookup.lookupClass)

  private def getArtWork = try
    futureLoad.get
  catch {
    case e@(_: InterruptedException | _: ExecutionException) =>
      logger.error("Failed to load ArtWork Proxy:", e)
      e.printStackTrace()
      throw new RuntimeException(e)
  }

  override def getFormat: String = format

  override def setFormat(format: String): Unit = getArtWork.setFormat(format)

  override def getCrc32: Long = getArtWork.getCrc32

  override def setCrc32(crc32: Long): Unit = getArtWork.setCrc32(crc32)

  override def getFileName: String = getArtWork.getFileName

  override def setFileName(fileName: String): Unit = getArtWork.setFileName(fileName)
}