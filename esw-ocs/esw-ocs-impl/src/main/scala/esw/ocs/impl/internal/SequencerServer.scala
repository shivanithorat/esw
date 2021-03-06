package esw.ocs.impl.internal

import akka.Done
import csw.location.api.models.AkkaLocation
import csw.prefix.models.{Prefix, Subsystem}
import esw.ocs.api.models.ObsMode
import esw.ocs.api.protocol.ScriptError

// Note: The APIs in this service are blocking. SequenceComponentBehavior consumes this api and since
// these operations are not performed very often they could be blocked. Blocking here
// significantly simplifies the design of SequenceComponentBehavior.
trait SequencerServer {
  def start(): Either[ScriptError, AkkaLocation]
  def shutDown(): Done
}

trait SequencerServerFactory {
  def make(subsystem: Subsystem, obsMode: ObsMode, sequenceComponentPrefix: Prefix): SequencerServer
}
