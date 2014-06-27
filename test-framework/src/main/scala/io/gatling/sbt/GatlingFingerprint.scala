package io.gatling.sbt

import sbt.testing.SubclassFingerprint

import io.gatling.core.scenario.Simulation

sealed abstract class GatlingFingerprint extends SubclassFingerprint {

  override val superclassName = classOf[Simulation].getName

  override val requireNoArgConstructor = true
}

class GatlingClassFingerprint extends GatlingFingerprint { val isModule = false }

class GatlingObjectFingerprint extends GatlingFingerprint { val isModule = true }
