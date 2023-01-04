/*
 * Copyright 2011-2023 GatlingCorp (https://gatling.io)
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

import _root_.io.gatling.sbt.ParserMatchers
import _root_.io.gatling.sbt.utils.ReportUtils._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import sbt._

class ReportUtilsSpec extends AnyFlatSpec with Matchers with ParserMatchers {
  {
    val reports = Seq(
      Report(new File("."), "basicsimulation-20140207205545", "basicsimulation", "20140207205545"),
      Report(new File("."), "othersimulation-20140207205545", "othersimulation", "20140207205545"),
      Report(new File("."), "othersimulation-20140207235545", "othersimulation", "20140207235545")
    )

    "filterReportsIfSimulationIdSelected" should "not filter reports if there's not selected simulation" in {
      filterReportsIfSimulationIdSelected(reports, None) shouldBe reports
    }

    it should "filter reports if there's a selected simulation" in {
      filterReportsIfSimulationIdSelected(reports, Some("basicsimulation")) shouldBe reports.take(1)
    }
  }

  {
    val reports = Seq(
      Report(new File("."), "basicsimulation-20140207205545", "basicsimulation", "20140207205545"),
      Report(new File("."), "othersimulation-20140207205545", "othersimulation", "20140207205545"),
      Report(new File("."), "othersimulation-20140207235545", "othersimulation", "20140207235545")
    )

    "filterReportsIfReportNameIdSelected" should "not filter reports if there's not selected simulation" in {
      filterReportsIfReportNameIdSelected(reports, None) shouldBe reports
    }

    it should "filter reports if there's a selected simulation" in {
      filterReportsIfReportNameIdSelected(reports, Some("basicsimulation-20140207205545")) shouldBe reports.take(1)
    }
  }

  {
    val simulationIds = Set("basicsimu", "basicsimulation", "advancedsimulation")
    val parser = simulationIdParser(simulationIds)

    "simulationIdParser" should "accept that no simulation ID is specified" in {
      parser should parse("")
    }

    it should "parse any simulation ID" in {
      parser should parse(" basicsimu")
      parser should parse(" basicsimulation")
      parser should parse(" advancedsimulation")
    }

    it should "complete simulation IDs as intended" in {
      parser should complete(" b", Set("asicsimu", "asicsimulation"))
      parser should complete(" basicsimul", Set("ation"))
      parser should complete(" a", Set("dvancedsimulation"))
    }
  }

  {
    val simulationIds = Set("basicsimu-20140404205455", "basicsimulation-20140408205455", "advancedsimulation-20140404205455")
    val parser = reportNameParser(simulationIds)

    "reportNameParser" should "accept that no simulation ID is specified" in {
      parser should parse("")
    }

    it should "parse any simulation ID" in {
      parser should parse(" basicsimu")
      parser should parse(" basicsimulation")
      parser should parse(" advancedsimulation")
    }

    it should "complete simulation IDs as intended" in {
      parser should complete(" b", Set("asicsimu-20140404205455", "asicsimulation-20140408205455"))
      parser should complete(" basicsimul", Set("ation-20140408205455"))
      parser should complete(" a", Set("dvancedsimulation-20140404205455"))
    }
  }

  "allReports" should "find the list of all reports" in {
    val root = new File("src/test/resources/reports")
    val foundReports = allReports(root)
    val expectedReports = Seq(
      Report(root / "basicsimulation-20140404205455", "basicsimulation-20140404205455", "basicsimulation", "20140404205455"),
      Report(root / "basicsimulation-20140406205455", "basicsimulation-20140406205455", "basicsimulation", "20140406205455"),
      Report(root / "basicsimulation-20140408205455", "basicsimulation-20140408205455", "basicsimulation", "20140408205455"),
      Report(root / "advancedsimulation-20140404205455", "advancedsimulation-20140404205455", "advancedsimulation", "20140404205455")
    )

    foundReports should contain theSameElementsAs expectedReports
  }

  "allSimulationIds" should "return the list of all found simulation IDs" in {
    val simulationIds = allSimulationIds(new File("src/test/resources/reports"))

    simulationIds should contain only ("basicsimulation", "advancedsimulation")
  }

  "allReportsNames" should "return the list of all found report names" in {
    val reportNames = allReportNames(new File("src/test/resources/reports"))

    reportNames should contain only (
      "basicsimulation-20140404205455", "basicsimulation-20140406205455",
      "basicsimulation-20140408205455", "advancedsimulation-20140404205455"
    )
  }
}
