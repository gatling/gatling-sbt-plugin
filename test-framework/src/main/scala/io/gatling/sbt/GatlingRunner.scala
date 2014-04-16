package io.gatling.sbt

import sbt.testing.{ Runner, TaskDef }

class GatlingRunner(val args: Array[String], val remoteArgs: Array[String], testClassLoader: ClassLoader) extends Runner {

  def tasks(taskDefs: Array[TaskDef]) = taskDefs.map(new GatlingTask(_, testClassLoader, args, remoteArgs))

  def done = "Simulation(s) execution ended."

}
