package uk.yermak.audiobookconverter

import java.io.{File, IOException}
import java.util.Collections
import java.util.concurrent._

import javafx.application.Platform
import javafx.collections.ObservableList
import net.bramp.ffmpeg.FFprobe
import org.apache.commons.io.FileUtils

import scala.collection.mutable.ListBuffer
import scala.collection.{JavaConverters, mutable}

/**
  * Created by yermak on 1/10/2018.
  */
object FFMediaLoader {
  private val mediaExecutor = Executors.newSingleThreadExecutor
  private val artExecutor = Executors.newScheduledThreadPool(4)

  private object MediaInfoCallable {
    private val AUDIO_CODECS = Set("mp3", "aac", "wmav2")
    private val ART_WORK_CODECS = Map("mjpeg" -> "jpg", "png" -> "png", "bmp" -> "bmp")
  }

  private class MediaInfoCallable(var ffprobe: FFprobe, val filename: String, var conversion: Conversion) extends Callable[MediaInfo] {
    @throws[Exception]
    override def call = try {
      if (conversion.getStatus.isOver) throw new InterruptedException("Media Info Loading was interrupted")
      val probeResult = ffprobe.probe(filename)
      val format = probeResult.getFormat
      val mediaInfo = new MediaInfoBean(filename)
      val streams = probeResult.getStreams
      import scala.collection.JavaConversions._
      for (ffMpegStream <- streams) {
        if (MediaInfoCallable.AUDIO_CODECS.contains(ffMpegStream.codec_name)) {
          mediaInfo.setCodec(ffMpegStream.codec_name)
          mediaInfo.setChannels(ffMpegStream.channels)
          mediaInfo.setFrequency(ffMpegStream.sample_rate)
          mediaInfo.setBitrate(ffMpegStream.bit_rate.toInt)
          mediaInfo.setDuration(ffMpegStream.duration.toLong * 1000)
        }
        else if (MediaInfoCallable.ART_WORK_CODECS.keySet.contains(ffMpegStream.codec_name)) {
          val futureLoad = artExecutor.schedule(new FFMediaLoader.ArtWorkCallable(mediaInfo, MediaInfoCallable.ART_WORK_CODECS(ffMpegStream.codec_name), conversion), 1, TimeUnit.SECONDS)
          val artWork = new ArtWorkProxy(futureLoad, MediaInfoCallable.ART_WORK_CODECS(ffMpegStream.codec_name))
          mediaInfo.setArtWork(artWork)
        }
      }

      val bookInfo = AudioBookInfo.instance(JavaConverters.mapAsScalaMap(format.tags))
      mediaInfo.setBookInfo(bookInfo)
      mediaInfo
    } catch {
      case e: IOException =>
        e.printStackTrace()
        throw e
    }
  }

  private object ArtWorkCallable {
    private val FFMPEG = new File("external/x64/ffmpeg.exe").getAbsolutePath
  }

  private class ArtWorkCallable(var mediaInfo: MediaInfoBean, var format: String, var conversion: Conversion) extends Callable[ArtWork] {
    @throws[Exception]
    override def call = {
      var process: Process = null
      try {
        if (conversion.getStatus.isOver) throw new InterruptedException("ArtWork loading was interrupted")
        val poster = Utils.getTmp(mediaInfo.hashCode, mediaInfo.hashCode, "." + format)
        val pictureProcessBuilder = new ProcessBuilder(ArtWorkCallable.FFMPEG, "-i", mediaInfo.getFileName, poster)
        process = pictureProcessBuilder.start
        StreamCopier.copy(process.getInputStream, System.out)
        // not using redirectErrorStream() as sometimes error stream is not closed by process which cause feature to hang indefinitely
        StreamCopier.copy(process.getErrorStream, System.err)
        var finished = false
        while ( {
          !conversion.getStatus.isOver && !finished
        }) finished = process.waitFor(500, TimeUnit.MILLISECONDS)
        val posterFile = new File(poster)
        val crc32 = Utils.checksumCRC32(posterFile)
        val artWorkBean = new ArtWorkBean(poster, format, crc32)
        Platform.runLater(() => addPosterIfMissing(artWorkBean, conversion.getPosters))
        artWorkBean
      } finally Utils.closeSilently(process)
    }
  }

  private[audiobookconverter] def findPictures(dir: File) =
    JavaConverters.collectionAsScalaIterable(FileUtils.listFiles(dir, Array[String]("jpg", "jpeg", "png", "bmp"), true))

  private[audiobookconverter] def searchForPosters(media: ListBuffer[MediaInfo], posters: ObservableList[ArtWork]) = {
    var searchDirs = mutable.Set[File]()
    media.foreach(mi => searchDirs += new File(mi.getFileName).getParentFile)
    searchDirs.foreach(d => findPictures(d).foreach(f => addPosterIfMissing(new ArtWorkBean(tempCopy(f.getPath), extension(f.getName), Utils.checksumCRC32(f)), posters)))
  }

  private[audiobookconverter] def addPosterIfMissing(artWork: ArtWork, posters: ObservableList[ArtWork]) =
    if (!JavaConverters.collectionAsScalaIterable(posters).exists(_.getCrc32 == artWork.getCrc32))
      posters.add(artWork)


  private[audiobookconverter] def extension(fileName: String) = {
    val i = fileName.lastIndexOf('.')
    fileName.substring(i)
  }

  private[audiobookconverter] def tempCopy(fileName: String) = {
    val destFile = new File(Utils.getTmp(System.currentTimeMillis, 0, extension(fileName)))
    try {
      FileUtils.copyFile(new File(fileName), destFile)
      destFile.getPath
    } catch {
      case e: IOException =>
        throw new RuntimeException(e)
    }
  }
}

class FFMediaLoader(var fileNames: java.util.List[String], var conversion: Conversion) {
  Collections.sort(fileNames)

  def loadMediaInfo: java.util.List[MediaInfo] =
    try {
      val media: ListBuffer[MediaInfo] = ListBuffer()
      fileNames.forEach(fileName => {
        val futureLoad = FFMediaLoader.mediaExecutor.submit(new FFMediaLoader.MediaInfoCallable(FFProbeSingleton.instance, fileName, conversion))
        media += new MediaInfoProxy(fileName, futureLoad)
      })
      FFMediaLoader.searchForPosters(media, conversion.getPosters)
      JavaConverters.bufferAsJavaList(media)
    } catch {
      case e: Exception => e.printStackTrace()
        throw new RuntimeException(e)
    }
}

object FFProbeSingleton {
  private val FFPROBE = new File("external/x64/ffprobe.exe").getAbsolutePath
  val instance: FFprobe = new FFprobe(FFPROBE)
}