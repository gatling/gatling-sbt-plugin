package io.gatling.sbt

import org.specs2.mutable.Specification

import io.gatling.sbt.StartRecorderUtils._

// TODO : test 'optionsParser'
class StartRecorderUtilsSpec extends Specification with ParserMatchers {

  "helpParser" should {
    "parse '-h' and '--help'" in {
      helpParser should complete(" ", Set("-h", "--help"))
      helpParser should parse(" -h")
      helpParser should parse(" --help")
    }
  }

  "optionParser" should {
    "correctly parses input according to prefix and options" in {
      val prefix = "--"
      val options = Set("about", "align", "build", "clean")
      val parser = optionParser(prefix, options)

      parser should complete(" ", options.map(prefix + _))
      parser should parse(" --about aaa")
      parser should parse(" --align 2")
      parser should parse(" --build foo.aa")
      parser should parse(" --clean bar")
      parser should not(complete(" --", Set("delete")))
    }
  }

  "toShortOptionAndValue" should {
    "properly convert the (option,value) pair to the corresponding arg list" in {
      toShortOptionAndValue(("lp", "8080")) should beEqualTo(Seq("-lp", "8080"))
    }
  }

  "addPackageIfNecessary" should {
    "not add the package if it has already been specified" in {
      addPackageIfNecessary(Seq("-pkg", "io.gatling"), "foo") should contain(exactly("-pkg", "io.gatling"))
      addPackageIfNecessary(Seq("--package", "io.gatling"), "foo") should contain(exactly("--package", "io.gatling"))
    }
    "add the package if it hasn't already been specified" in {
      addPackageIfNecessary(Seq.empty, "foo") should contain(exactly("-pkg", "foo"))
    }
  }
}
