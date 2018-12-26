package uk.yermak.audiobookconverter

import java.io.{File, IOException}
import java.util

import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.StringUtils

import scala.collection.JavaConverters._

class MetadataBuilder {
  @throws[IOException]
  protected def prepareMeta(jobId: Long,
                            bookInfo: AudioBookInfo,
                            inputMedia: util.List[MediaInfo]): File = {
    val metaFile =
      new File(System.getProperty("java.io.tmpdir"), "FFMETADATAFILE" + jobId)

    val media = inputMedia.asScala.toList

    val metaData = new util.ArrayList[String]
    metaData.add(";FFMETADATA1")
    metaData.add("major_brand=M4A")
    metaData.add("minor_version=512")
    metaData.add("compatible_brands=isomiso2")
    metaData.add("title=" + bookInfo.getTitle)
    metaData.add("artist=" + bookInfo.getWriter)

    if (StringUtils.isNotBlank(bookInfo.getSeries)) {
      metaData.add("album=" + bookInfo.getSeries)
    } else {
      metaData.add("album=" + bookInfo.getTitle)
    }

    metaData.add("composer=" + bookInfo.getNarrator)
    metaData.add("comment=" + bookInfo.getComment)
    metaData.add("track=" + bookInfo.getBookNumber + "/" + bookInfo.getTotalTracks)
    metaData.add("media_type=2")
    metaData.add("genre=" + bookInfo.getGenre)
    metaData.add("encoder=" + "https://github.com/yermak/AudioBookConverter")
    var totalDuration:Long = 0
    var i = 1

    for (m <- media) {
      metaData.add("[CHAPTER]")
      metaData.add("TIMEBASE=1/1000")
      metaData.add("START=" + totalDuration)
      totalDuration += m.getDuration()
      metaData.add("END=" + totalDuration)

      if (bookInfo.getBookNumber != 0) {
        metaData.add("title=Book " + bookInfo.getBookNumber + " : Chapter: " + i)
      }
      else {
        metaData.add("title=Chapter: " + i)
      }
      i += 1
    }
    FileUtils.writeLines(metaFile, "UTF-8", metaData)
    metaFile
  }
}
