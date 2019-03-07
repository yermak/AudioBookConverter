package uk.yermak.audiobookconverter

import java.io.{File, IOException}
import java.net.URISyntaxException
import java.util.concurrent.TimeUnit

import net.bramp.ffmpeg.progress.{Progress, TcpProgressParser}

/**
  * Created by Yermak on 29-Dec-17.
  */
object FFMpegConcatenator {
  private val FFMPEG = new File("external/x64/ffmpeg.exe").getAbsolutePath
}

class FFMpegConcatenator(var conversion: Conversion, val outputFileName: String, var metaDataFileName: String, var fileListFileName: String, var callback: ProgressCallback) {
  private var progressParser: TcpProgressParser = _

  @throws[IOException]
  @throws[InterruptedException]
  def concat(): Unit = {
    if (conversion.getStatus.isOver) return
    while ( {
      ProgressStatus.PAUSED == conversion.getStatus
    }) Thread.sleep(1000)
    callback.reset()
    try {
      progressParser = new TcpProgressParser((progress: Progress) => callback.converted(progress.out_time_ns / 1000000, progress.total_size))
      progressParser.start()
    } catch {
      case e: URISyntaxException =>
    }
    var process: Process = null
    try {
      val ffmpegProcessBuilder = new ProcessBuilder(FFMpegConcatenator.FFMPEG, "-protocol_whitelist", "file,pipe,concat", "-vn", "-f", "concat", "-safe", "0", "-i", fileListFileName, "-i", metaDataFileName, "-map_metadata", "1", "-f", "ipod", "-c:a", "copy", "-movflags", "+faststart", "-progress", progressParser.getUri.toString, outputFileName)
      process = ffmpegProcessBuilder.start
      StreamCopier.copy(process.getInputStream, System.out)
      StreamCopier.copy(process.getErrorStream, System.err)
      var finished = false
      while ( {
        !conversion.getStatus.isOver && !finished
      }) finished = process.waitFor(500, TimeUnit.MILLISECONDS)
    } finally {
      Utils.closeSilently(process)
      Utils.closeSilently(progressParser)
    }
  }
}