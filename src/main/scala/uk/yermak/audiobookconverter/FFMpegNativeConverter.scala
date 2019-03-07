package uk.yermak.audiobookconverter

import java.io.{File, IOException}
import java.net.URISyntaxException
import java.util.concurrent.{Callable, CancellationException, TimeUnit}

import net.bramp.ffmpeg.progress.{Progress, TcpProgressParser}

/**
  * Created by Yermak on 29-Dec-17.
  */
object FFMpegNativeConverter {
  private val FFMPEG = new File("external/x64/ffmpeg.exe").getAbsolutePath
}

class FFMpegNativeConverter(var conversion: Conversion, var outputParameters: OutputParameters, var mediaInfo: MediaInfo, val outputFileName: String, var callback: ProgressCallback) extends Callable[ConverterOutput] {
  private var process: Process = _
  private var progressParser: TcpProgressParser = _

  @throws[IOException]
  @throws[InterruptedException]
  def convertMp3toM4a: ConverterOutput = try {
    if (conversion.getStatus.isOver) return null
    while ( {
      ProgressStatus.PAUSED == conversion.getStatus
    }) Thread.sleep(1000)
    progressParser = new TcpProgressParser((progress: Progress) => {
      def foo(progress: Progress) = {
        callback.converted(progress.out_time_ns / 1000000, progress.total_size)
        if (progress.isEnd) callback.completedConversion()
      }

      foo(progress)
    })
    progressParser.start()
    var ffmpegProcessBuilder: ProcessBuilder = null
    if (outputParameters.isAuto) if (mediaInfo.getCodec == "aac") ffmpegProcessBuilder = new ProcessBuilder(FFMpegNativeConverter.FFMPEG, "-i", mediaInfo.getFileName, "-vn", "-codec:a", "copy", "-f", "ipod", "-progress", progressParser.getUri.toString, outputFileName)
    else ffmpegProcessBuilder = new ProcessBuilder(FFMpegNativeConverter.FFMPEG, "-i", mediaInfo.getFileName, "-vn", "-codec:a", "aac", "-f", "ipod", "-progress", progressParser.getUri.toString, outputFileName)
    else ffmpegProcessBuilder = new ProcessBuilder(FFMpegNativeConverter.FFMPEG, "-i", mediaInfo.getFileName, "-vn", "-codec:a", "aac", "-f", "ipod", outputParameters.getFFMpegQualityParameter, outputParameters.getFFMpegQualityValue, "-ar", String.valueOf(outputParameters.getFFMpegFrequencyValue), "-ac", String.valueOf(outputParameters.getFFMpegChannelsValue), "-cutoff", outputParameters.getCutoffValue, "-progress", progressParser.getUri.toString, outputFileName)
    process = ffmpegProcessBuilder.start
    val ffmpegIn = process.getInputStream
    val ffmpegErr = process.getErrorStream
    StreamCopier.copy(ffmpegIn, System.out)
    StreamCopier.copy(ffmpegErr, System.err)
    var finished = false
    while ( {
      !conversion.getStatus.isOver && !finished
    }) finished = process.waitFor(500, TimeUnit.MILLISECONDS)
    new ConverterOutput(mediaInfo, outputFileName)
  } catch {
    case ce: CancellationException =>
      null
    case e: URISyntaxException =>
      throw new RuntimeException(e)
  } finally {
    Utils.closeSilently(process)
    Utils.closeSilently(progressParser)
  }

  @throws[Exception]
  override def call = convertMp3toM4a
}