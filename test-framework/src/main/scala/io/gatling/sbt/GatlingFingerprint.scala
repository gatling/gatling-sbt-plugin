package io.gatling.sbt

import sbt.testing.SubclassFingerprint

import io.gatling.core.scenario.Simulation

/**
 * Gatling's specific fingerprint, which defines which classes are to be
 * picked up by the test framework from the test ClassLoader as test classes,
 * in this case Gatling simulations.
 */
class GatlingFingerprint extends SubclassFingerprint {

  /** Matches only Scala classes, as simulation objects are not supported. */
  val isModule = false

  /** All classes that are to be picked up must extend ''Simulation'' */
  override val superclassName = classOf[Simulation].getName

  /** Gatling simulations does not take constructor arguments. */
  override val requireNoArgConstructor = true
}
