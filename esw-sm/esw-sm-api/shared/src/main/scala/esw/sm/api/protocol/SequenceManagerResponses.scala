package esw.sm.api.protocol

import csw.location.api.models.ComponentId
import csw.prefix.models.Prefix
import esw.ocs.api.models.ObsMode
import esw.sm.api.codecs.SmAkkaSerializable
import esw.sm.api.protocol.ShutdownSequencerResponse.UnloadScriptError

private[protocol] sealed trait SmFailure extends Throwable

sealed trait ConfigureResponse extends SmAkkaSerializable

object ConfigureResponse {
  case class Success(masterSequencerComponentId: ComponentId) extends ConfigureResponse

  sealed trait Failure                                                            extends SmFailure with ConfigureResponse
  case class ConflictingResourcesWithRunningObsMode(runningObsMode: Set[ObsMode]) extends Failure
  case class FailedToStartSequencers(reasons: Set[String])                        extends Failure
}

sealed trait GetRunningObsModesResponse extends SmAkkaSerializable

object GetRunningObsModesResponse {
  case class Success(runningObsModes: Set[ObsMode]) extends GetRunningObsModesResponse
  case class Failed(msg: String)                    extends SmFailure with GetRunningObsModesResponse
}

sealed trait CleanupResponse extends SmAkkaSerializable

object CleanupResponse {
  case object Success extends CleanupResponse

  sealed trait Failure                                         extends SmFailure with CleanupResponse
  case class FailedToShutdownSequencers(response: Set[String]) extends Failure
}

sealed trait StartSequencerResponse extends SmAkkaSerializable

object StartSequencerResponse {
  sealed trait Success                                extends StartSequencerResponse
  case class Started(componentId: ComponentId)        extends Success
  case class AlreadyRunning(componentId: ComponentId) extends Success

  sealed trait Failure extends SmFailure with StartSequencerResponse with RestartSequencerResponse.Failure {
    def msg: String
  }
  case class LoadScriptError(msg: String) extends Failure
}

sealed trait ShutdownSequencerResponse extends SmAkkaSerializable

object ShutdownSequencerResponse {
  case object Success extends ShutdownSequencerResponse

  sealed trait Failure extends SmFailure with ShutdownSequencerResponse with RestartSequencerResponse.Failure {
    def msg: String
  }
  case class UnloadScriptError(prefix: Prefix, msg: String) extends Failure
}

sealed trait ShutdownAllSequencersResponse extends SmAkkaSerializable
object ShutdownAllSequencersResponse {
  case object Success extends ShutdownAllSequencersResponse

  sealed trait Failure                                                  extends SmFailure with ShutdownAllSequencersResponse
  case class ShutdownFailure(failureResponses: List[UnloadScriptError]) extends ShutdownAllSequencersResponse.Failure
}

sealed trait RestartSequencerResponse extends SmAkkaSerializable

object RestartSequencerResponse {
  case class Success(componentId: ComponentId) extends RestartSequencerResponse

  sealed trait Failure extends SmFailure with RestartSequencerResponse {
    def msg: String
  }
}

sealed trait SpawnSequenceComponentResponse extends SmAkkaSerializable

object SpawnSequenceComponentResponse {
  case class Success(componentId: ComponentId) extends SpawnSequenceComponentResponse

  sealed trait Failure extends SmFailure with SpawnSequenceComponentResponse
}

sealed trait ShutdownSequenceComponentResponse extends SmAkkaSerializable
object ShutdownSequenceComponentResponse {
  case object Success extends ShutdownSequenceComponentResponse

  sealed trait Failure                                                     extends SmFailure with ShutdownSequenceComponentResponse
  case class ShutdownSequenceComponentFailure(prefix: Prefix, msg: String) extends Failure
}

sealed trait CommonFailure extends SmFailure with ConfigureResponse.Failure with CleanupResponse.Failure

object CommonFailure {
  case class ConfigurationMissing(obsMode: ObsMode) extends CommonFailure
  case class LocationServiceError(msg: String)
      extends AgentError
      with CommonFailure
      with ShutdownSequencerResponse.Failure
      with ShutdownAllSequencersResponse.Failure
      with ShutdownSequenceComponentResponse.Failure
}

sealed trait AgentError extends StartSequencerResponse.Failure with SpawnSequenceComponentResponse.Failure

object AgentError {
  case class SpawnSequenceComponentFailed(msg: String) extends AgentError
}
