package io.gatling.sbt.utils

import java.util.jar.JarFile

import scala.collection.JavaConversions._

import sbt._

object CopyUtils {

  def extractFromCoreJar[T](updateReport: UpdateReport, fileName: String)(f: File => T) =
    extractFromJar(updateReport, "gatling-core", fileName)(f)

  def extractFromRecorderJar[T](updateReport: UpdateReport, fileName: String)(f: File => T) =
    extractFromJar(updateReport, "gatling-recorder", fileName)(f)

  private def extractFromJar[T](updateReport: UpdateReport, jarName: String, fileName: String)(f: File => T) = {
    val jar = getJarFromUpdateReport(updateReport, jarName)
    withFileInJar(jar, fileName)(f)
  }

  private def getJarFromUpdateReport(updateReport: UpdateReport, name: String) =
    updateReport.select(artifact = artifactFilter(new ExactFilter(name))).headOption match {
      case Some(jar) => jar
      case None =>
        throw new IllegalStateException(s"Could not find $name jar in dependencies. Please add it to your dependencies.")
    }

  private def withFileInJar[T](jarPath: File, fileName: String)(f: File => T) = {
    val jarFile = new JarFile(jarPath)
    val possibleEntry = jarFile.entries().find(_.getName.endsWith(fileName))
    possibleEntry match {
      case Some(entry) =>
        IO.withTemporaryFile("copy", "sbttemp") { file =>
          IO.write(file, IO.readBytes(jarFile.getInputStream(entry)))
          f(file)
        }
      case None =>
        throw new IllegalArgumentException(s"Could not find entry with fileName $fileName in jar $jarPath")
    }
  }
}
