package io.gatling.sbt

import sbt.testing.{ Runner, TaskDef }

/**
 * As there is no further special handling needed or simulations to reject,
 * [[GatlingRunner]] simply creates a [[GatlingTask]] for each discovered simulation.
 *
 *  @param args the arguments for the new run.
 * @param remoteArgs the arguments for the run in a forked JVM.
 * @param testClassLoader the test ClassLoader, provided by SBT.
 */
class GatlingRunner(val args: Array[String], val remoteArgs: Array[String], testClassLoader: ClassLoader) extends Runner {

  def tasks(taskDefs: Array[TaskDef]) = taskDefs.map(new GatlingTask(_, testClassLoader, args, remoteArgs))

  def done = "Simulation(s) execution ended."

}
