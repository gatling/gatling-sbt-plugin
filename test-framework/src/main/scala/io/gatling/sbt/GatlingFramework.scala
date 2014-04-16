package io.gatling.sbt

import sbt.testing.{ Fingerprint, Framework }

class GatlingFramework extends Framework {

  val name = "gatling"

  val fingerprints = Array[Fingerprint](new GatlingClassFingerprint, new GatlingObjectFingerprint)

  def runner(args: Array[String], remoteArgs: Array[String], testClassLoader: ClassLoader) =
    new GatlingRunner(args, remoteArgs, testClassLoader)

}
