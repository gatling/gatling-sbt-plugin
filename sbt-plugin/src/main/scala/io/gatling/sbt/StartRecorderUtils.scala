package io.gatling.sbt

import sbt.complete.Parser
import sbt.complete.DefaultParsers._

object StartRecorderUtils {

  /**
   * List of all CLI options supported by the Recorder,
   * in their "short" version.
   */
  private val shortRecorderOpts = Set(
    "lp", "lps", "ph", "pp", "pps", "ar",
    "rbf", "cn", "pkg", "enc", "fr", "fhr")

  /**
   * List of all CLI options supported by the Recorder,
   * in their "full" version.
   */
  private val fullRecorderOpts = Set(
    "local-port", "local-port-ssl", "proxy-host",
    "proxy-port", "proxy-port-ssl", "request-bodies-folder",
    "class-name", "package", "encoding", "follow-redirect",
    "automatic-referer", "fetch-html-resources")

  /** Parser matching the help option, in short and full version. */
  val helpParser: Parser[Seq[String]] = (token(Space) ~> ("-h" | "--help")) map { s => Seq(s)}

  /**
   * Builds a parser matching any option from ''options'', prefixed by ''prefix''.
   * @param prefix the option prefix.
   * @param options the list of supported options.
   * @return the built parser.
   */
  def optionParser(prefix: String, options: Set[String]): Parser[Seq[String]] = {
    // Match a string provided in examples, prefixed by the provided prefix
    val option = (prefix ~ NotSpace.examples(options, check = true)) map { case (s1, s2) => s1 + s2}
    // Match the option and the provided arg, with necessary spaces dropped from the parsed result
    token(Space) ~> ((option <~ token(Space)) ~ NotSpace) map { case (s1, s2) => List(s1, s2)}
  }

  /**
   * The complete option parser, matching any option supported by the Recorder,
   * whether it is its full or short version.
   */
  val optionsParser: Parser[Seq[String]] =
    helpParser | ((optionParser("-", shortRecorderOpts) | optionParser("--", fullRecorderOpts)).* map (_.flatten))

  /**
   * Transforms a pair (short option, value) to the corresponding list of arguments.
   *
   * @param optionAndValue the (short option, value) pair.
   * @return the corresponding list of arguments.
   */
  def toShortOptionAndValue(optionAndValue: (String, String)): Seq[String] =
    optionAndValue match {
      case (arg, value) => List("-" + arg, value)
    }

  /**
   * Adds the ''package'' option, set to default to ''packageName'',
   * if its hasn't already been specified by the user.
   *
   * @param args the current list of arguments
   * @param packageName the default package name
   * @return the list of arguments, with the default package set if it wasn't already set.
   */
  def addPackageIfNecessary(args: Seq[String], packageName: String) =
    if (args.contains("-pkg") || args.contains("--package")) args
    else args ++ toShortOptionAndValue("pkg" -> packageName)
}
