package uk.yermak.audiobookconverter

import java.io.{File, IOException, PrintWriter, StringWriter}
import java.util.concurrent.{ExecutionException, Executors, Future}
import java.util.stream.{Collectors, IntStream}

import org.apache.commons.io.FileUtils

import scala.collection.JavaConverters
import scala.collection.mutable.ListBuffer

class BatchConversionStrategy(var conversion: Conversion, var progressCallbacks: Map[String, ProgressCallback]) extends ConversionStrategy {
  final private val executorService = Executors.newWorkStealingPool

  protected def getTempFileName(jobId: Long, index: Int, extension: String) = ""

  override def run() = {
    val futures: ListBuffer[Future[ConverterOutput]] = ListBuffer()
    try {
      val media = JavaConverters.collectionAsScalaIterable(conversion.getMedia)
      media.foreach((mediaInfo: MediaInfo) => {
        val outputFileName = this.determineOutputFilename(mediaInfo.getFileName)
        val converterFuture = executorService.submit(new FFMpegNativeConverter(conversion, conversion.getOutputParameters, mediaInfo, outputFileName, progressCallbacks(mediaInfo.getFileName)))
        futures += converterFuture
      }
      )

      val artBuilder = new Mp4v2ArtBuilder(conversion)

      futures.foreach((future: Future[ConverterOutput]) => {
        val output = future.get
        val artWork = output.getMediaInfo.getArtWork
        if (artWork != null) artBuilder.updateSinglePoster(artWork, 0, output.getOutputFileName)
      })
      conversion.finished()
    }

    catch {
      case e@(_: InterruptedException | _: ExecutionException | _: IOException) =>
        val sw = new StringWriter
        e.printStackTrace(new PrintWriter(sw))
        conversion.error(e.getMessage + "; " + sw.getBuffer.toString)
    }
  }

  private def determineOutputFilename(inputFilename: String) = {
    var outputFilename: String = null
    if (conversion.getOutputDestination == null) outputFilename = inputFilename.replaceAll("(?i)\\.mp3", ".m4b")
    else {
      val file = new File(inputFilename)
      val outFile = new File(conversion.getOutputDestination, file.getName)
      outputFilename = outFile.getAbsolutePath.replaceAll("(?i)\\.mp3", ".m4b")
    }
    if (!outputFilename.endsWith(".m4b")) outputFilename = outputFilename + ".m4b"
    Utils.makeFilenameUnique(outputFilename)
  }

  @throws[IOException]
  protected def prepareFiles(jobId: Long) = {
    val fileListFile = new File(System.getProperty("java.io.tmpdir"), "filelist." + jobId + ".txt")
    val outFiles = IntStream.range(0, conversion.getMedia.size).mapToObj((i: Int) => "file '" + getTempFileName(jobId, i, ".m4b") + "'").collect(Collectors.toList[String])
    FileUtils.writeLines(fileListFile, "UTF-8", outFiles)
    fileListFile
  }
}