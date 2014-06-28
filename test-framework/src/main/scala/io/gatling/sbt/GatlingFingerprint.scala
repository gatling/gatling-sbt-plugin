package io.gatling.sbt

import sbt.testing.SubclassFingerprint

import io.gatling.core.scenario.Simulation

class GatlingFingerprint extends SubclassFingerprint {

  val isModule = false

  override val superclassName = classOf[Simulation].getName

  override val requireNoArgConstructor = true
}
