package uk.yermak.audiobookconverter

object Version {
  val MAJOR = 3
  val MINOR = 2
  val BUILD = 1

  def getVersionString: String = { //TODO add load version from the build number.
    "AudioBookConverter " + MAJOR + "." + MINOR + (if (BUILD != 0) "." + BUILD
    else "")
  }
}

class Version() {
}