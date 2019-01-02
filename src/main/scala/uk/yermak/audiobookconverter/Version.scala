package uk.yermak.audiobookconverter

object Version {
  val MAJOR = 3
  val MINOR = 1
  val BUILD = 0

  def getVersionString: String = { //TODO add load version from the build number.
    "AudioBookConverter " + MAJOR + "." + MINOR + (if (BUILD != 0) "." + BUILD
    else "")
  }
}

class Version() {
}