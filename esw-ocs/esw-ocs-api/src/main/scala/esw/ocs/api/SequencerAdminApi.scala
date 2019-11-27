package esw.ocs.api

import csw.command.api.scaladsl.SequencerCommandService
import csw.params.commands.CommandResponse.SubmitResponse
import csw.params.commands.{Sequence, SequenceCommand}
import csw.params.core.models.Id
import csw.time.core.models.UTCTime
import esw.ocs.api.models.StepList
import esw.ocs.api.protocol._

import scala.concurrent.Future

trait SequencerAdminApi extends SequencerCommandService {

  //Editor API
  def getSequence: Future[Option[StepList]]
  def isAvailable: Future[Boolean]
  def isOnline: Future[Boolean]
  def add(commands: List[SequenceCommand]): Future[OkOrUnhandledResponse]
  def prepend(commands: List[SequenceCommand]): Future[OkOrUnhandledResponse]
  def replace(id: Id, commands: List[SequenceCommand]): Future[GenericResponse]
  def insertAfter(id: Id, commands: List[SequenceCommand]): Future[GenericResponse]
  def delete(id: Id): Future[GenericResponse]
  def pause: Future[PauseResponse]
  def resume: Future[OkOrUnhandledResponse]
  def addBreakpoint(id: Id): Future[GenericResponse]
  def removeBreakpoint(id: Id): Future[RemoveBreakpointResponse]
  def reset(): Future[OkOrUnhandledResponse]
  def abortSequence(): Future[OkOrUnhandledResponse]
  def stop(): Future[OkOrUnhandledResponse]
  def loadSequence(sequence: Sequence): Future[OkOrUnhandledResponse]
  def startSequence(): Future[SubmitResponse]

  //Lifecycle API
  def goOnline(): Future[GoOnlineResponse]
  def goOffline(): Future[GoOfflineResponse]
  def diagnosticMode(startTime: UTCTime, hint: String): Future[DiagnosticModeResponse]
  def operationsMode(): Future[OperationsModeResponse]
}
