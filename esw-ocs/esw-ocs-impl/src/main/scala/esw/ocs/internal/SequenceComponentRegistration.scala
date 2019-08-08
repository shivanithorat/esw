package esw.ocs.internal

import akka.actor.typed.SpawnProtocol.Spawn
import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.scaladsl.adapter.TypedActorSystemOps
import akka.actor.typed.{ActorRef, ActorSystem, SpawnProtocol}
import akka.actor.{CoordinatedShutdown, Scheduler}
import akka.util.Timeout
import csw.location.api.exceptions.OtherLocationIsRegistered
import csw.location.api.extensions.ActorExtension.RichActor
import csw.location.api.extensions.URIExtension.RichURI
import csw.location.api.scaladsl.{LocationService, RegistrationResult}
import csw.location.models.Connection.AkkaConnection
import csw.location.models.{AkkaLocation, AkkaRegistration, ComponentId, ComponentType}
import csw.params.core.models.Prefix
import esw.ocs.api.models.messages.SequenceComponentMsg.Stop
import esw.ocs.api.models.messages.{RegistrationError, SequenceComponentMsg}
import esw.ocs.core.SequenceComponentBehavior
import esw.ocs.syntax.FutureSyntax.FutureOps
import esw.utils.csw.LocationServiceUtils

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

class SequenceComponentRegistration(prefix: Prefix, locationService: LocationService, locationServiceUtils: LocationServiceUtils)(
    implicit actorSystem: ActorSystem[SpawnProtocol]
) {
  implicit val ec: ExecutionContext = actorSystem.executionContext
  implicit val scheduler: Scheduler = actorSystem.scheduler
  implicit val timeout: Timeout     = Timeout(Timeouts.DefaultTimeout)

  private def addCoordinatedShutdownTask(
      coordinatedShutdown: CoordinatedShutdown,
      registrationResult: RegistrationResult
  ): Unit = {
    coordinatedShutdown.addTask(
      CoordinatedShutdown.PhaseBeforeServiceUnbind,
      s"unregistering-${registrationResult.location}"
    )(() => registrationResult.unregister())
  }

  def registerWithRetry(retryCount: Int): Future[Either[RegistrationError, AkkaLocation]] = {
    val akkaRegistration = registration()
    locationService
      .register(akkaRegistration)
      .map { result =>
        addCoordinatedShutdownTask(CoordinatedShutdown(actorSystem.toUntyped), result)
        Right(result.location.asInstanceOf[AkkaLocation])
      }
      .recoverWith {
        case OtherLocationIsRegistered(_) if retryCount > 0 =>
          //kill actor ref if registration fails. Retry attempt will create new actor ref
          akkaRegistration.actorRefURI.toActorRef.unsafeUpcast[SequenceComponentMsg] ! Stop
          registerWithRetry(retryCount - 1)
        case NonFatal(e) => Future.successful(Left(RegistrationError(e.getMessage)))
      }
  }

  private def generateSequenceComponentName(): String = {
    val subsystem = prefix.subsystem
    locationServiceUtils
      .listBy(subsystem, ComponentType.SequenceComponent)
      .map { sequenceComponents =>
        val uniqueId = s"${sequenceComponents.length + 1}"
        s"${subsystem}_$uniqueId"
      }
      .block
  }

  private def registration(): AkkaRegistration = {
    val sequenceComponentName = generateSequenceComponentName()
    val sequenceComponentRef: ActorRef[SequenceComponentMsg] =
      (actorSystem ? Spawn(SequenceComponentBehavior.behavior(sequenceComponentName), sequenceComponentName)).block
    AkkaRegistration(
      AkkaConnection(ComponentId(sequenceComponentName, ComponentType.SequenceComponent)),
      prefix,
      sequenceComponentRef.toURI
    )
  }

}
