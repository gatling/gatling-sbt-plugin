import sbt._

object Resolvers {

	private val sonatypeRoot = "https://oss.sonatype.org/"

	val sonatypeSnapshots = "Sonatype Snapshots" at sonatypeRoot + "content/repositories/snapshots/"
}
