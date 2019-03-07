package uk.yermak.audiobookconverter

import java.io.{File, IOException, PrintWriter, StringWriter}
import java.util.stream.{Collectors, IntStream}

import org.apache.commons.io.FileUtils

class JoiningConversionStrategy(var conversion: Conversion, var progressCallbacks: Map[String, ProgressCallback]) extends ConversionStrategy {
  protected def getTempFileName(jobId: Long, index: Int, extension: String) = conversion.getMedia.get(index).getFileName

  override def run(): Unit = {
    val jobId = System.currentTimeMillis
    val tempFile = Utils.getTmp(jobId, 999999, ".m4b")
    var metaFile: File = null
    var fileListFile: File = null
    try {
      conversion.getOutputParameters.updateAuto(conversion.getMedia)
      metaFile = new MetadataBuilder().prepareMeta(jobId, conversion.getBookInfo, conversion.getMedia)
      fileListFile = prepareFiles(jobId)
      if (conversion.getStatus.isOver) return
      val concatenator = new FFMpegLinearNativeConverter(conversion, tempFile, metaFile.getAbsolutePath, fileListFile.getAbsolutePath, conversion.getOutputParameters, progressCallbacks("output"))
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
      FileUtils.deleteQuietly(metaFile)
      FileUtils.deleteQuietly(fileListFile)
    }
  }

  @throws[IOException]
  protected def prepareFiles(jobId: Long) = {
    val fileListFile = new File(System.getProperty("java.io.tmpdir"), "filelist." + jobId + ".txt")
    val outFiles = IntStream.range(0, conversion.getMedia.size).mapToObj(i => "file '" + getTempFileName(jobId, i, ".m4b") + "'").collect(Collectors.toList[String])
    FileUtils.writeLines(fileListFile, "UTF-8", outFiles)
    fileListFile
  }
}