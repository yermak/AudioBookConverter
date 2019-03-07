package uk.yermak.audiobookconverter

import java.io.IOException
import java.net.URISyntaxException
import java.util.concurrent.TimeUnit

import net.bramp.ffmpeg.progress.{Progress, TcpProgressParser}

/**
  * Created by Yermak on 29-Dec-17.
  */
class FFMpegLinearNativeConverter(var conversion: Conversion, val outputFileName: String, var metaDataFileName: String, var fileListFileName: String, var outputParameters: OutputParameters, var callback: ProgressCallback) {
  private var ffmpegProcess: Process = _
  private var progressParser: TcpProgressParser = _

  @throws[IOException]
  @throws[InterruptedException]
  def concat(): Unit = {
    if (conversion.getStatus.isOver) return
    while ( {
      ProgressStatus.PAUSED == conversion.getStatus
    }) Thread.sleep(1000)
    try {
      progressParser = new TcpProgressParser((progress: Progress) => {
        def foo(progress: Progress) = {
          callback.converted(progress.out_time_ns / 1000000, progress.total_size)
          if (progress.isEnd) callback.completedConversion()
        }

        foo(progress)
      })
      progressParser.start()
    } catch {
      case e: URISyntaxException =>
        e.printStackTrace()
    }
    try {
      var ffmpegProcessBuilder: ProcessBuilder = null
      if (outputParameters.isAuto) if (conversion.getMedia.stream.allMatch((mediaInfo: MediaInfo) => mediaInfo.getCodec == "aac")) ffmpegProcessBuilder = new ProcessBuilder("external/x64/ffmpeg.exe", "-protocol_whitelist", "file,pipe,concat", "-f", "concat", "-safe", "0", "-i", fileListFileName, "-i", metaDataFileName, "-map_metadata", "1", "-vn", "-f", "ipod", "-codec:a", "copy", "-progress", progressParser.getUri.toString, outputFileName)
      else ffmpegProcessBuilder = new ProcessBuilder("external/x64/ffmpeg.exe", "-protocol_whitelist", "file,pipe,concat", "-f", "concat", "-safe", "0", "-i", fileListFileName, "-i", metaDataFileName, "-map_metadata", "1", "-vn", outputParameters.getFFMpegQualityParameter, outputParameters.getFFMpegQualityValue, "-ar", outputParameters.getFFMpegFrequencyValue, "-ac", outputParameters.getFFMpegChannelsValue, "-cutoff", outputParameters.getCutoffValue, "-f", "ipod", "-codec:a", "aac", "-progress", progressParser.getUri.toString, outputFileName)
      else ffmpegProcessBuilder = new ProcessBuilder("external/x64/ffmpeg.exe", "-protocol_whitelist", "file,pipe,concat", "-f", "concat", "-safe", "0", "-i", fileListFileName, "-i", metaDataFileName, "-map_metadata", "1", "-vn", outputParameters.getFFMpegQualityParameter, outputParameters.getFFMpegQualityValue, "-ar", outputParameters.getFFMpegFrequencyValue, "-ac", outputParameters.getFFMpegChannelsValue, "-cutoff", outputParameters.getCutoffValue, "-f", "ipod", "-codec:a", "aac", "-progress", progressParser.getUri.toString, outputFileName)
      ffmpegProcess = ffmpegProcessBuilder.start
      StreamCopier.copy(ffmpegProcess.getInputStream, System.out)
      StreamCopier.copy(ffmpegProcess.getErrorStream, System.err)
      var finished = false
      while ( {
        !conversion.getStatus.isOver && !finished
      }) finished = ffmpegProcess.waitFor(500, TimeUnit.MILLISECONDS)
    } finally {
      Utils.closeSilently(ffmpegProcess)
      Utils.closeSilently(progressParser)
    }
  }
}