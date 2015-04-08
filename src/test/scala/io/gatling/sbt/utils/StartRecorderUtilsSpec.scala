package io.gatling.sbt.utils

import io.gatling.sbt.ParserMatchers
import io.gatling.sbt.utils.StartRecorderUtils._

import org.scalatest.{ FlatSpec, Matchers }

class StartRecorderUtilsSpec extends FlatSpec with Matchers with ParserMatchers {

  "helpParser" should "parse '-h' and '--help'" in {
    helpParser should complete(" ", Set("-h", "--help"))
    helpParser should parse(" -h")
    helpParser should parse(" --help")
  }

  "optionParser" should "correctly parses input according to prefix and options" in {
    val prefix = "--"
    val options = Set("about", "align", "build", "clean")
    val parser = optionParser(prefix, options)

    parser should complete(" ", options.map(prefix + _))
    parser should complete(" --a", Set("bout", "lign"))
    parser should parse(" --about aaa")
    parser should parse(" --align 2")
    parser should parse(" --build foo.aa")
    parser should parse(" --clean bar")
    parser should not(parse(" --delete aaa"))
  }

  "optionsParser" should "accept that no args are specified" in {
    optionsParser should parse("")
  }

  it should "parse all short and full arguments" in {
    val allCompletions = shortRecorderOpts ++ fullRecorderOpts.map("-" + _) ++ Set("h", "-help")
    optionsParser should complete(" -", allCompletions)
  }

  "toShortOptionAndValue" should "properly convert the (option,value) pair to the corresponding arg list" in {
    toShortOptionAndValue(("lp", "8080")) shouldBe Seq("-lp", "8080")
  }

  "addPackageIfNecessary" should "not add the package if it has already been specified" in {
    addPackageIfNecessary(Seq("-pkg", "io.gatling"), "foo") should contain only ("-pkg", "io.gatling")
    addPackageIfNecessary(Seq("--package", "io.gatling"), "foo") should contain only ("--package", "io.gatling")
  }

  it should "add the package if it hasn't already been specified" in {
    addPackageIfNecessary(Seq.empty, "foo") should contain only ("-pkg", "foo")
  }

  "exactStringParser" should "creates a parser that matches only strings from the specified choices" in {
    val parser = exactStringParser(Set("foo", "bar"))

    parser should parse("foo")
    parser should parse("bar")
    parser should not(parse("quz"))
  }
}
