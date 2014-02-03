package io.gatling.sbt

import java.awt.Desktop
import java.net.URI

object Utils {

	val reportFolderRegex = "(\\w+)-(\\d+)".r

	def toShortArgumentList(args: Map[String, String]): Seq[String] = {
		args.toList flatMap { case (arg, value) => List("-" + arg, value) }
	}

	def openInBrowser(location: URI): Unit = {
		if (!Desktop.isDesktopSupported) {
			throw new UnsupportedOperationException("Opening a report from SBT is currently not supported on your platform.")
		} else {
			val desktop = Desktop.getDesktop
			desktop.browse(location)
		}
	}
}
