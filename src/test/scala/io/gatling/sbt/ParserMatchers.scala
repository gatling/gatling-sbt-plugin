package io.gatling.sbt

import scala.language.implicitConversions

import org.specs2.matcher.{ Expectable, Matcher, MatchResult }

import sbt.complete.{ Completion, Completions, Parser }
import sbt.complete.DefaultParsers.{ completions, matches }

trait ParserMatchers {

  def parse(expected: String) = new Matcher[Parser[_]] {
    override def apply[S <: Parser[_]](t: Expectable[S]): MatchResult[S] =
      result(matches(t.value, expected),
        s"${t.description} parses $expected",
        s"${t.description} does not parse $expected",
        t)
  }

  def complete(string: String, expected: Completions) = new Matcher[Parser[_]] {
    override def apply[S <: Parser[_]](t: Expectable[S]): MatchResult[S] =
      result(completions(t.value, string, 1) == expected,
        s"${t.description} completes $string with $expected",
        s"${t.description} does not complete $string with $expected",
        t)
  }

  implicit def stringSetToCompletion(set: Set[String]): Completions =
    Completions.strict(set.map(Completion.suggestion(_)))
}
