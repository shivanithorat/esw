package esw

import akka.actor.typed.{ActorSystem, SpawnProtocol}
import csw.framework.ShellWiring
import csw.location.api.models.ComponentType.Service
import esw.sm.api.SequenceManagerApi
import esw.sm.api.actor.client.SequenceManagerImpl
import shell.utils.Extensions.FutureExt

class EswWiring {
  lazy val shellWiring = new ShellWiring

  implicit lazy val typedSystem: ActorSystem[SpawnProtocol.Command] = shellWiring.wiring.actorSystem

  private lazy val locationUtils = new LocationUtils(shellWiring.cswContext.locationService)
  lazy val commandServiceDsl     = new CommandServiceDsl(locationUtils)

  def sequenceManager(): SequenceManagerApi =
    locationUtils
      .findAkkaLocation("ESW.sequence_manager", Service)
      .map(new SequenceManagerImpl(_))(typedSystem.executionContext)
      .await()
}
