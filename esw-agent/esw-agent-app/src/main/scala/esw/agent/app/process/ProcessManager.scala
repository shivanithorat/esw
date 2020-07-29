package esw.agent.app.process

import java.util.concurrent.atomic.AtomicReference

import akka.Done
import akka.actor.CoordinatedShutdown
import akka.actor.CoordinatedShutdown.PhaseBeforeServiceUnbind
import akka.actor.typed.{ActorRef, ActorSystem}
import csw.location.api.models._
import csw.location.api.scaladsl.{LocationService, RegistrationResult}
import csw.logging.api.scaladsl.Logger
import esw.agent.api.AgentCommand.SpawnCommand.SpawnManuallyRegistered.SpawnRedis
import esw.agent.api.AgentCommand.SpawnCommand.SpawnSelfRegistered.SpawnSequenceComponent
import esw.agent.api.AgentCommand.SpawnCommand.{SpawnManuallyRegistered, SpawnSelfRegistered}
import esw.agent.api.AgentCommand.{ProcessExited, SpawnCommand}
import esw.agent.api.ComponentStatus.{Initializing, Running, Stopping}
import esw.agent.api._
import esw.agent.app.AgentSettings
import esw.agent.app.ext.FutureEitherExt.FutureEitherOps
import esw.agent.app.ext.ProcessExt.ProcessOps
import esw.agent.app.process.cs.Coursier
import esw.agent.app.process.redis.Redis

import scala.concurrent.Future
import scala.concurrent.duration.{DurationLong, FiniteDuration}
import scala.util.Try
import scala.util.control.NonFatal

class ProcessManager(
    locationService: LocationService,
    processExecutor: ProcessExecutor,
    agentSettings: AgentSettings,
    logger: Logger,
    command: SpawnCommand
)(implicit system: ActorSystem[_]) {
  import agentSettings._
  import command._
  import system.executionContext

  private val status        = new AtomicReference[ComponentStatus](Initializing)
  private val process       = new AtomicReference[Option[Process]](None)
  private val componentName = componentId.fullName

  private val executableCommand: List[String] = command match {
    case SpawnSequenceComponent(_, _, version) => Coursier.ocsApp(version).launch(coursierChannel, commandArgs)
    case _: SpawnRedis                         => Redis.server :: commandArgs
  }

  def spawn(agentActor: ActorRef[AgentCommand]): Future[SpawnResponse] =
    verifyComponentIsNotAlreadyRegistered
      .flatMapE(_ => startComponent(agentActor))
      .flatMapE(_ => waitForComponentRegistration.flatMapE(_ => reconcile))
      .mapToAdt(_ => Spawned, e => { logger.warn(e); Failed(e) })

  def getStatus: ComponentStatus = status.get()

  // un-registration is done as a part of process.onComplete callback
  def kill: Future[KillResponse] = {
    status.set(Stopping)
    killProcess.map(_ => Killed).recover {
      case NonFatal(e) => Failed(s"Failed to kill component $componentName, reason: $e")
    }
  }

  private def verifyComponentIsNotAlreadyRegistered: Future[Either[String, Unit]] =
    checkRegistration(0.seconds) {
      case None    => Right(())
      case Some(l) => Left(s"Component $componentName is already registered with location service at location $l")
    }

  private def startComponent(agentActor: ActorRef[AgentCommand]) =
    Future.successful(
      processExecutor.runCommand(executableCommand, prefix).map { p => process.set(Some(p)); onProcessExit(p, agentActor) }
    )

  private def reconcile =
    if (process.get().isEmpty) {
      status.set(Stopping)
      unregisterComponent.transform(_ => Try(Left("Process terminated before registration was successful")))
    }
    else { status.set(Running); Future.successful(Right(())) }

  private def waitForComponentRegistration: Future[Either[String, Unit]] =
    command match {
      case _: SpawnSelfRegistered       => waitForRegistration(durationToWaitForComponentRegistration)
      case cmd: SpawnManuallyRegistered => registerComponent(cmd.registration)
    }

  private def addUnRegistrationHook(result: RegistrationResult): Unit =
    CoordinatedShutdown(system)
      .addTask(PhaseBeforeServiceUnbind, s"unregister-${prefix.componentName}") { () =>
        result.unregister()
      }

  private def registerComponent(registration: Registration): Future[Either[String, Unit]] =
    locationService.register(registration).map { r => Right(addUnRegistrationHook(r)) }.recover {
      case NonFatal(e) => Left(s"Failed to register component $componentName with location service, reason: ${e.getMessage}")
    }

  private def unregisterComponent: Future[Done] = locationService.unregister(connection)

  private def checkRegistration(
      timeout: FiniteDuration
  )(check: Option[Location] => Either[String, Unit]): Future[Either[String, Unit]] =
    locationService.resolve(connection.of[Location], timeout).map(check).recover {
      case NonFatal(e) => Left(s"Failed to verify component registration in location service, reason: ${e.getMessage}")
    }

  private def waitForRegistration(timeout: FiniteDuration): Future[Either[String, Unit]] =
    checkRegistration(timeout) {
      case Some(_) => Right(())
      case None    => Left(s"Component $componentName is not registered with location service")
    }

  private def onProcessExit(p: Process, agentActor: ActorRef[AgentCommand]): Unit =
    p.onComplete { _ =>
      process.set(None)
      logger.warn(s"Process exited with exit value: ${p.exitValue()}, unregistering component $componentName")
      agentActor ! ProcessExited(componentId)
      unregisterComponent
    }

  private def killProcess: Future[Unit] =
    process.get().fold(Future.successful(()))(_.kill(10.seconds).map(_ => process.set(None)))
}
