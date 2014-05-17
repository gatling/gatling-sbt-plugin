package io.gatling.sbt

import sbt.complete.Parser
import sbt.complete.DefaultParsers._

object StartRecorderUtils {

  private val shortRecorderArgs = Set(
    "h", "lp", "lps", "ph", "pp", "pps",
    "rbf", "cn", "pkg", "enc", "fr")

  private val fullRecorderArgs = Set(
    "help", "local-port", "local-port-ssl", "proxy-host",
    "proxy-port", "proxy-port-ssl", "request-bodies-folder",
    "class-name", "package", "encoding", "follow-redirect")

  def argParser(prefix: String, examples: Set[String]): Parser[Seq[String]] = {
    // Match a string provided in examples, prefixed by the provided prefix
    val option = (prefix ~ NotSpace.examples(examples, check = true)) map { case (s1, s2) => s1 + s2 }
    // Match the option and the provided arg, with necessary spaces dropped from the parsed result
    token(Space) ~> ((option <~ token(Space)) ~ NotSpace) map { case (s1, s2) => List(s1, s2) }
  }

  val argsParser: Parser[Seq[String]] =
    (argParser("-", shortRecorderArgs) | argParser("--", fullRecorderArgs)).* map (_.flatten)

  def toShortArgument(argument: (String, String)): Seq[String] =
    argument match { case (arg, value) => List("-" + arg, value) }

  def addPackageIfNecessary(args: Seq[String], packageName: String) =
    if (args.contains("-pkg") || args.contains("--package"))
      args
    else args ++ toShortArgument("pkg" -> packageName)
}
