package esw.commons

import scala.concurrent.duration.{DurationLong, FiniteDuration}

object Timeouts {
  val DefaultTimeout: FiniteDuration = 30.seconds
  val LongTimeout: FiniteDuration    = 10.hours
}