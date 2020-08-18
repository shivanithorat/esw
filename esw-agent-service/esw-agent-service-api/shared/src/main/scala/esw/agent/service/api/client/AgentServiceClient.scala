package esw.agent.service.api.client

import java.nio.file.Path

import csw.location.api.models.ComponentId
import csw.prefix.models.Prefix
import esw.agent.service.api.AgentServiceApi
import esw.agent.service.api.codecs.AgentHttpCodecs
import esw.agent.service.api.models.{KillResponse, SpawnResponse}
import esw.agent.service.api.protocol.AgentPostRequest
import esw.agent.service.api.protocol.AgentPostRequest.{SpawnSequenceComponent, SpawnSequenceManager, KillComponent}
import msocket.api.Transport

import scala.concurrent.Future

class AgentServiceClient(postClient: Transport[AgentPostRequest]) extends AgentServiceApi with AgentHttpCodecs {

  override def spawnSequenceManager(
      agentPrefix: Prefix,
      obsModeConfigPath: Path,
      isConfigLocal: Boolean,
      version: Option[String]
  ): Future[SpawnResponse] =
    postClient.requestResponse[SpawnResponse](SpawnSequenceManager(agentPrefix, obsModeConfigPath, isConfigLocal, version))

  override def spawnSequenceComponent(
      agentPrefix: Prefix,
      componentName: String,
      version: Option[String]
  ): Future[SpawnResponse] =
    postClient.requestResponse[SpawnResponse](SpawnSequenceComponent(agentPrefix, componentName, version))

  override def killComponent(agentPrefix: Prefix, componentId: ComponentId): Future[KillResponse] =
    postClient.requestResponse[KillResponse](KillComponent(agentPrefix, componentId))
}
