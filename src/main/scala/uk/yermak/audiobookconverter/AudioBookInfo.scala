package uk.yermak.audiobookconverter

import javafx.collections.ObservableList

class AudioBookInfo() {
  private var writer = ""
  private var narrator = ""
  private var title = ""
  private var series = ""
  private var genre = ""
  private var year = ""
  private var bookNumber = 0
  private var totalTracks = 0
  private var comment = ""
  private var longDescription = ""
  private var posters:ObservableList[ArtWork] = null

  def getSeries: String = {
    if (series == null) return title
    this.series
  }

  def setSeries(series: String): Unit = this.series = series

  def getWriter: String = this.writer

  def setWriter(writer: String): Unit = this.writer = writer

  def getComment: String = this.comment

  def setComment(comment: String): Unit = this.comment = comment

  def getGenre: String = this.genre

  def setGenre(genre: String): Unit = this.genre = genre

  def getTitle: String = this.title

  def setTitle(title: String): Unit = this.title = title

  def getBookNumber: Int = this.bookNumber

  def setBookNumber(bookNumber: Int): Unit = this.bookNumber = bookNumber

  def getNarrator: String = this.narrator

  def setNarrator(narrator: String): Unit = this.narrator = narrator

  def getYear: String = this.year

  def setYear(year: String): Unit = this.year = year

  def getTotalTracks: Int = totalTracks

  def setTotalTracks(totalTracks: Int): Unit = this.totalTracks = totalTracks

  def getLongDescription: String = longDescription

  def setLongDescription(longDescription: String): Unit = this.longDescription = longDescription

  def getPosters: ObservableList[ArtWork] = posters

  def setPosters(posters: ObservableList[ArtWork]): Unit = this.posters = posters
}