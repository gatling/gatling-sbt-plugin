package io.gatling.sbt

object Utils {

	def toShortArgumentList(args: Map[String, String]): Seq[String] = {
		args.toList flatMap { case (arg, value) => List("-" + arg, value) }
	}
}