package io.gatling.sbt

import sbt.File
import java.awt.{ Desktop, GraphicsEnvironment }
import java.net.URI

object Utils {

  val reportFolderRegex = "(\\w+)-(\\d+)".r

  def toShortArgumentList(args: Map[String, String]): Seq[String] = {
    args.toList flatMap { case (arg, value) => List("-" + arg, value) }
  }

  def openInBrowser(location: URI): Unit = {
    if (!Desktop.isDesktopSupported || GraphicsEnvironment.isHeadless) {
      throw new UnsupportedOperationException("Opening a report from SBT is currently not supported on your platform.")
    } else {
      val desktop = Desktop.getDesktop
      desktop.browse(location)
    }
  }

  case class Report(path: File, simulationId: String, timestamp: String)

  implicit val reportOrdering = Ordering.fromLessThan[Report](_.timestamp > _.timestamp)
}
