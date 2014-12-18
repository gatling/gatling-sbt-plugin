package io.gatling.sbt.utils

import sbt._
import org.specs2.mutable.Specification

import io.gatling.sbt.ParserMatchers
import io.gatling.sbt.utils.ReportUtils._

class ReportUtilsSpec extends Specification with ParserMatchers {

  "filterReportsIfSimulationIdSelected" should {
    val reports = Seq(
      Report(new File("."), "basicsimulation-20140207205545", "basicsimulation", "20140207205545"),
      Report(new File("."), "othersimulation-20140207205545", "othersimulation", "20140207205545"),
      Report(new File("."), "othersimulation-20140207235545", "othersimulation", "20140207235545"))

    "not filter reports if there's not selected simulation" in {
      filterReportsIfSimulationIdSelected(reports, None) should beEqualTo(reports)
    }
    "filter reports if there's a selected simulation" in {
      filterReportsIfSimulationIdSelected(reports, Some("basicsimulation")) should beEqualTo(reports.take(1))
    }
  }

  "filterReportsIfReportNameIdSelected" should {
    val reports = Seq(
      Report(new File("."), "basicsimulation-20140207205545", "basicsimulation", "20140207205545"),
      Report(new File("."), "othersimulation-20140207205545", "othersimulation", "20140207205545"),
      Report(new File("."), "othersimulation-20140207235545", "othersimulation", "20140207235545"))

    "not filter reports if there's not selected simulation" in {
      filterReportsIfReportNameIdSelected(reports, None) should beEqualTo(reports)
    }
    "filter reports if there's a selected simulation" in {
      filterReportsIfReportNameIdSelected(reports, Some("basicsimulation-20140207205545")) should beEqualTo(reports.take(1))
    }
  }

  "simulationIdParser" should {
    val simulationIds = Set("basicsimu", "basicsimulation", "advancedsimulation")
    val parser = simulationIdParser(simulationIds)

    "accept that no simulation ID is specified" in {
      parser should parse("")
    }

    "parse any simulation ID" in {
      parser should parse(" basicsimu")
      parser should parse(" basicsimulation")
      parser should parse(" advancedsimulation")
    }

    "complete simulation IDs as intended" in {
      parser should complete(" b", Set("asicsimu", "asicsimulation"))
      parser should complete(" basicsimul", Set("ation"))
      parser should complete(" a", Set("dvancedsimulation"))
    }
  }

  "reportNameParser" should {
    val simulationIds = Set("basicsimu-20140404205455", "basicsimulation-20140408205455", "advancedsimulation-20140404205455")
    val parser = reportNameParser(simulationIds)

    "accept that no simulation ID is specified" in {
      parser should parse("")
    }

    "parse any simulation ID" in {
      parser should parse(" basicsimu")
      parser should parse(" basicsimulation")
      parser should parse(" advancedsimulation")
    }

    "complete simulation IDs as intended" in {
      parser should complete(" b", Set("asicsimu-20140404205455", "asicsimulation-20140408205455"))
      parser should complete(" basicsimul", Set("ation-20140408205455"))
      parser should complete(" a", Set("dvancedsimulation-20140404205455"))
    }
  }

  "allReports" should {
    "find the list of all reports" in {
      val root = new File("src/test/resources/reports")
      val foundReports = allReports(root)
      val expectedReports = Seq(
        Report(root / "basicsimulation-20140404205455", "basicsimulation-20140404205455", "basicsimulation", "20140404205455"),
        Report(root / "basicsimulation-20140406205455", "basicsimulation-20140406205455", "basicsimulation", "20140406205455"),
        Report(root / "basicsimulation-20140408205455", "basicsimulation-20140408205455", "basicsimulation", "20140408205455"),
        Report(root / "advancedsimulation-20140404205455", "advancedsimulation-20140404205455", "advancedsimulation", "20140404205455"))

      foundReports must containTheSameElementsAs(expectedReports)
    }
  }

  "allSimulationIds" should {
    "return the list of all found simulation IDs" in {
      val simulationIds = allSimulationIds(new File("src/test/resources/reports"))

      simulationIds should contain(exactly("basicsimulation", "advancedsimulation"))
    }
  }

  "allReportsNames" should {
    "return the list of all found report names" in {
      val reportNames = allReportNames(new File("src/test/resources/reports"))

      reportNames should contain(exactly(
        "basicsimulation-20140404205455", "basicsimulation-20140406205455",
        "basicsimulation-20140408205455", "advancedsimulation-20140404205455"))
    }
  }
}
