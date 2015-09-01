/**
 * Copyright 2011-2015 GatlingCorp (http://gatling.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gatling.sbt.utils

import sbt.complete.DefaultParsers._
import sbt.complete.Parser

private[gatling] object StartRecorderUtils {

  /**
   * List of all CLI options supported by the Recorder,
   * in their "short" version.
   */
  val shortRecorderOpts = Set(
    "lp", "lps", "ph", "pp", "pps", "ar",
    "rbf", "cn", "pkg", "enc", "fr", "fhr"
  )

  /**
   * List of all CLI options supported by the Recorder,
   * in their "full" version.
   */
  val fullRecorderOpts = Set(
    "local-port", "local-port-ssl", "proxy-host",
    "proxy-port", "proxy-port-ssl", "request-bodies-folder",
    "class-name", "package", "encoding", "follow-redirect",
    "automatic-referer", "fetch-html-resources"
  )

  /** Parser matching the help option, in short and full version. */
  val helpParser: Parser[Seq[String]] = (token(Space) ~> exactStringParser(Set("-h", "--help"))) map { s => Seq(s) }

  /**
   * Builds a parser matching any option from ''options'', prefixed by ''prefix''.
   * @param prefix the option prefix.
   * @param options the list of supported options.
   * @return the built parser.
   */
  def optionParser(prefix: String, options: Set[String]): Parser[Seq[String]] = {
    // Match a string provided in examples, prefixed by the provided prefix
    val option = (prefix ~ exactStringParser(options)) map { case (s1, s2) => s1 + s2 }
    // Match the option and the provided arg, with necessary spaces dropped from the parsed result
    token(Space) ~> ((option <~ token(Space)) ~ NotSpace) map { case (s1, s2) => List(s1, s2) }
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

  /**
   * Creates a parser that matches exactly one of the the strings from ''choices''
   * @param choices the list of strings to match
   * @return the built parser.
   */
  def exactStringParser(choices: Set[String]): Parser[String] =
    choices.map(_.id).reduceLeft(_ | _.id)
}
