package io.gatling.sbt

import scala.language.implicitConversions

import org.scalatest.matchers.{ MatchResult, Matcher }
import sbt.complete.DefaultParsers.{ completions, matches }
import sbt.complete.{ Completion, Completions, Parser }

trait ParserMatchers {

  def parse(expected: String) = new Matcher[Parser[_]] {
    override def apply(parser: Parser[_]) =
      MatchResult(matches(parser, expected),
        s"$parser does not parse $expected",
        s"$parser parses $expected")
  }

  def complete(string: String, expected: Completions) = new Matcher[Parser[_]] {
    override def apply(parser: Parser[_]) =
      MatchResult(completions(parser, string, 1) == expected,
        s"$parser does not complete $string with $expected",
        s"$parser completes $string with $expected")
  }

  implicit def stringSetToCompletion(set: Set[String]): Completions =
    Completions.strict(set.map(Completion.suggestion(_)))
}
