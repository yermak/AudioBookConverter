package uk.yermak.audiobookconverter

import java.io.{File, IOException, PrintWriter, StringWriter}
import java.util
import java.util.Collections
import java.util.concurrent.{Executors, Future}

import org.apache.commons.io.FileUtils

object ParallelConversionStrategy {
  private val executorService = Executors.newWorkStealingPool
}

class ParallelConversionStrategy(var conversion: Conversion, var progressCallbacks: Map[String, ProgressCallback]) extends ConversionStrategy {
  override def run(): Unit = {
    val futures = new util.ArrayList[Future[ConverterOutput]]
    val jobId = System.currentTimeMillis
    val tempFile = Utils.getTmp(jobId, 999999, ".m4b")
    var fileListFile: File = null
    var metaFile: File = null
    try { //            MediaInfo maxMedia = maximiseEncodingParameters();
      conversion.getOutputParameters.updateAuto(conversion.getMedia)
      fileListFile = prepareFiles(jobId)
      val prioritizedMedia = prioritiseMedia
      import scala.collection.JavaConversions._
      for (mediaInfo <- prioritizedMedia) {
        val tempOutput = getTempFileName(jobId, mediaInfo.hashCode, ".m4b")
        val callback: ProgressCallback = progressCallbacks(mediaInfo.getFileName)
        val converterFuture = ParallelConversionStrategy.executorService.submit(new FFMpegNativeConverter(conversion, conversion.getOutputParameters, mediaInfo, tempOutput, callback))
        futures.add(converterFuture)
      }
      import scala.collection.JavaConversions._
      for (future <- futures) {
        if (conversion.getStatus.isOver) return
        future.get
      }
      if (conversion.getStatus.isOver) return
      metaFile = new MetadataBuilder().prepareMeta(jobId, conversion.getBookInfo, conversion.getMedia)
      val concatenator = new FFMpegConcatenator(conversion, tempFile, metaFile.getAbsolutePath, fileListFile.getAbsolutePath, progressCallbacks("output"))
      concatenator.concat()
      if (conversion.getStatus.isOver) return
      val artBuilder = new Mp4v2ArtBuilder(conversion)
      artBuilder.coverArt(tempFile)
      if (conversion.getStatus.isOver) return
      FileUtils.moveFile(new File(tempFile), new File(conversion.getOutputDestination))
      conversion.finished()
    } catch {
      case e: Exception =>
        e.printStackTrace()
        val sw = new StringWriter
        e.printStackTrace(new PrintWriter(sw))
        conversion.error(e.getMessage + "; " + sw.getBuffer.toString)
    } finally {
      import scala.collection.JavaConversions._
      for (mediaInfo <- conversion.getMedia) {
        FileUtils.deleteQuietly(new File(getTempFileName(jobId, mediaInfo.hashCode, ".m4b")))
      }
      FileUtils.deleteQuietly(metaFile)
      FileUtils.deleteQuietly(fileListFile)
      if (conversion.getStatus.isOver) return
    }
  }

  private def prioritiseMedia = {
    val sortedMedia = new util.ArrayList[MediaInfo](conversion.getMedia.size)
    import scala.collection.JavaConversions._
    for (mediaInfo <- conversion.getMedia) {
      sortedMedia.add(mediaInfo)
      //            mediaInfo.setFrequency(maxMedia.getFrequency());
      //            mediaInfo.setChannels(maxMedia.getChannels());
      //            mediaInfo.setBitrate(maxMedia.getBitrate());
    }
    Collections.sort(sortedMedia, (o1: MediaInfo, o2: MediaInfo) => (o2.getDuration - o1.getDuration).toInt)
    sortedMedia
  }

  protected def getTempFileName(jobId: Long, index: Int, extension: String) = Utils.getTmp(jobId, index, extension)

  @throws[IOException]
  protected def prepareFiles(jobId: Long) = {
    val fileListFile = new File(System.getProperty("java.io.tmpdir"), "filelist." + jobId + ".txt")
    var outFiles: java.util.List[String] = new util.ArrayList[String]()
    conversion.getMedia.forEach(mi => outFiles.add("file '" + getTempFileName(jobId, mi.hashCode, ".m4b") + "'"))


    //    val outFiles:java.util.List[String] = conversion.getMedia.stream.map((mediaInfo:MediaInfo) => "file '" + getTempFileName(jobId, mediaInfo.hashCode, ".m4b") + "'").collect(Collectors.toList[String])
    FileUtils.writeLines(fileListFile, "UTF-8", outFiles)
    fileListFile
  }
}