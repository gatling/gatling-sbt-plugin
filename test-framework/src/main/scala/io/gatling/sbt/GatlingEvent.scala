package io.gatling.sbt

import sbt.testing.{ Event, Fingerprint, OptionalThrowable, Selector, Status }

case class SimulationSuccessful(
    fullyQualifiedName: String,
    fingerprint: Fingerprint,
    selector: Selector,
    throwable: OptionalThrowable,
    duration: Long) extends Event {

  val status = Status.Success
}

case class SimulationFailed(
    fullyQualifiedName: String,
    fingerprint: Fingerprint,
    selector: Selector,
    throwable: OptionalThrowable,
    duration: Long) extends Event {

  val status = Status.Failure
}

case class InvalidArguments(
    fullyQualifiedName: String,
    fingerprint: Fingerprint,
    selector: Selector,
    throwable: OptionalThrowable,
    duration: Long) extends Event {

  val status = Status.Error
}
