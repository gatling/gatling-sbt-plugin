/*
 * Copyright 2011-2025 GatlingCorp (https://gatling.io)
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

import io.gatling.sbt.ParserMatchers
import io.gatling.sbt.utils.StartRecorderUtils._

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class StartRecorderUtilsSpec extends AnyFlatSpec with Matchers with ParserMatchers {
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
