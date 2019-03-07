package uk.yermak.audiobookconverter

import java.io.{File, FileInputStream, FileOutputStream}
import java.util.Properties

object AppProperties {
  private def getAppProperties = {
    val defaultProperties = new Properties
    val applicationProps = new Properties(defaultProperties)
    try {
      val in = new FileInputStream(new File(new File(System.getenv("APPDATA"), "AudioBookConverter-V3"), "AudioBookConverter-V3.properties"))
      applicationProps.load(in)
      in.close()
    } catch {
      case e: Exception =>
    }
    applicationProps
  }

  def getProperty(key: String) = {
    val applicationProps = getAppProperties
    applicationProps.getProperty(key)
  }

  def setProperty(key: String, value: String) = {
    val applicationProps = getAppProperties
    applicationProps.put(key, value)
    try {
      val appDir = new File(new File(System.getenv("APPDATA")), "AudioBookConverter-V3")
      if (!appDir.exists) {
        val succ = appDir.mkdir
        System.out.println(succ)
      }
      val out = new FileOutputStream(new File(appDir, "AudioBookConverter-V3.properties"))
      applicationProps.store(out, "")
      out.close()
    } catch {
      case var5: Exception =>
    }
  }
}

class AppProperties() {
}