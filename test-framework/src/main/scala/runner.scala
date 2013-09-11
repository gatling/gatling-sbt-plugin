//copied from orig gatling sources
package gatling.sbt

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit.SECONDS

import scala.concurrent.Await

import org.joda.time.DateTime.now

import com.typesafe.scalalogging.slf4j.Logging

import io.gatling.core.Predef._
import io.gatling.core.action.{ AkkaDefaults, system }
import io.gatling.core.action.system.dispatcher
import io.gatling.core.config.GatlingConfiguration.configuration
import io.gatling.core.result.message.RunMessage
import io.gatling.core.result.terminator.Terminator
import io.gatling.core.result.writer.DataWriter

import io.gatling.core.runner.Selection


class AdaptedRunner[S <: Simulation](simulation: S) extends AkkaDefaults with Logging {

  def run: (String, Simulation) = {

    try {
      val runMessage = RunMessage(now, "to-change", "to be changed")

      val scenarios = simulation.scenarios

      require(!scenarios.isEmpty, s"${simulation.getClass.getName} returned an empty scenario list. Did you forget to migrate your Simulations?")
      val scenarioNames = scenarios.map(_.name)
      require(scenarioNames.toSet.size == scenarioNames.size, s"Scenario names must be unique but found $scenarioNames")

      val totalNumberOfUsers = scenarios.map(_.injectionProfile.users).sum
      logger.info(s"Total number of users : $totalNumberOfUsers")

      val terminatorLatch = new CountDownLatch(1)

      val init = Terminator
        .askInit(terminatorLatch, totalNumberOfUsers)
        .flatMap(_ => DataWriter.askInit(runMessage, scenarios))

      Await.result(init, defaultTimeOut.duration)

      val runUUID = java.util.UUID.randomUUID.getMostSignificantBits
      logger.debug(s"Launching All Scenarios with UUID:$runUUID")

      scenarios.foldLeft(0) { (i, scenario) =>
        scenario.run(runUUID + "-", i)
        i + scenario.injectionProfile.users
      }
      logger.debug("Finished Launching scenarios executions")

      terminatorLatch.await(configuration.core.timeOut.simulation, SECONDS)
      println("Simulation finished.")

      (runMessage.runId, simulation)

    } finally {
      system.shutdown
    }
  }
}