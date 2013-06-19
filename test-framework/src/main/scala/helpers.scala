package gatling.sbt

import io.gatling.core.Predef._
import io.gatling.core.runner.{Runner, Selection}
//import com.excilys.ebi.gatling.core.scenario.configuration.ScenarioConfigurationBuilder

trait PerfTest {
  //this because Selection only takes Simulation's class.......
  def simulation:Class[Simulation] = test.getClass.asInstanceOf[Class[Simulation]]

  def test:Simulation

  def pre:Unit

  def exec(/*runInfo: RunRecord*/) = {
    //todo ... pre and post are crap for => think about a way to:
    // * give them info
    // * get info from them
    // * compose them?
    pre

    //val configurations = simulation.scenarios
    val selection = Selection(simulation, "test", "test")
    new Runner(selection).run

    post
  }

  def post:Unit
}