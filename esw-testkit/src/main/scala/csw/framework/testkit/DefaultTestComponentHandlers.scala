package csw.framework.testkit
import akka.actor.typed.scaladsl.ActorContext
import csw.command.client.messages.TopLevelActorMessage
import csw.framework.models.CswContext
import csw.framework.scaladsl.ComponentHandlers
import csw.location.api.models.TrackingEvent
import csw.params.commands.CommandResponse.{Accepted, Completed}
import csw.params.commands.{CommandResponse, ControlCommand}
import csw.params.core.models.Id
import csw.time.core.models.UTCTime

class DefaultTestComponentHandlers(ctx: ActorContext[TopLevelActorMessage], cswCtx: CswContext)
    extends ComponentHandlers(ctx, cswCtx) {

  /**
   * The initialize handler is invoked when the component is created. The component can initialize state such as configuration to be fetched
   * from configuration service, location of components or services to be fetched from location service etc. These vary
   * from component to component.
   *
   * @return when the initialization of component completes
   */
  override def initialize(): Unit = {}

  /**
   * The onLocationTrackingEvent handler can be used to take action on the TrackingEvent for a particular connection.
   * This event could be for the connections in ComponentInfo tracked automatically or for the connections tracked
   * explicitly using trackConnection method.
   *
   * @param trackingEvent represents a LocationUpdated or LocationRemoved event received for a tracked connection
   */
  override def onLocationTrackingEvent(trackingEvent: TrackingEvent): Unit = {}

  /**
   * The validateCommand is invoked when a command is received by this component.
   * The component is required to validate the ControlCommand received and return a validation result as Accepted or Invalid.
   *
   * @param runId Run ID for command tracking
   * @param controlCommand represents a command received e.g. Setup, Observe or wait
   * @return a CommandResponse after validation
   */
  override def validateCommand(
      runId: Id,
      controlCommand: ControlCommand
  ): CommandResponse.ValidateCommandResponse = Accepted(runId)

  /**
   * On receiving a command as Submit, the onSubmit handler is invoked for a component only if the validateCommand handler
   * returns Accepted. In case a command is received as a submit, command response should be updated in the CommandResponseManager.
   * CommandResponseManager is an actor whose reference commandResponseManager is available in the ComponentHandlers.
   *
   * @param runId Run ID for command tracking
   * @param controlCommand represents a command received e.g. Setup, Observe or wait
   * @return response for command submission
   */
  override def onSubmit(runId: Id, controlCommand: ControlCommand): CommandResponse.SubmitResponse =
    Completed(runId)

  /**
   * On receiving a command as Oneway, the onOneway handler is invoked for a component only if the validateCommand handler
   * returns Accepted. In case a command is received as a oneway, command response should not be provided to the sender.
   *
   * @param runId Run ID for command tracking
   * @param controlCommand represents a command received e.g. Setup, Observe or wait
   */
  override def onOneway(runId: Id, controlCommand: ControlCommand): Unit = {}

  /**
   * On receiving a diagnostic data command, the component goes into a diagnostic data mode based on hint at the specified startTime.
   * Validation of supported hints need to be handled by the component writer.
   *
   * @param startTime represents the time at which the diagnostic mode actions will take effect
   * @param hint      represents supported diagnostic data mode for a component
   */
  override def onDiagnosticMode(startTime: UTCTime, hint: String): Unit = {}

  /**
   * On receiving a operations mode command, the current diagnostic data mode is halted.
   */
  override def onOperationsMode(): Unit = {}

  /**
   * The onShutdown handler can be used for carrying out the tasks which will allow the component to shutdown gracefully
   *
   * @return when the shutdown completes for component
   */
  override def onShutdown(): Unit = {}

  /**
   * A component can be notified to run in offline mode in case it is not in use. The component can change its behavior
   * if needed as a part of this handler.
   */
  override def onGoOffline(): Unit = { isOnline = false }

  /**
   * A component can be notified to run in online mode again in case it was put to run in offline mode. The component can
   * change its behavior if needed as a part of this handler.
   */
  override def onGoOnline(): Unit = { isOnline = true }
}
