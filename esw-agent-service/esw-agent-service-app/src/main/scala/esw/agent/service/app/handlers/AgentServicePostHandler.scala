package esw.agent.service.app.handlers

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import csw.aas.http.SecurityDirectives
import esw.agent.service.api.AgentServiceApi
import esw.agent.service.api.codecs.AgentServiceCodecs._
import esw.agent.service.api.protocol.AgentServiceRequest
import esw.agent.service.api.protocol.AgentServiceRequest.{
  GetAgentStatus,
  KillComponent,
  SpawnContainers,
  SpawnSequenceComponent,
  SpawnSequenceManager
}
import esw.commons.auth.AuthPolicies
import msocket.http.post.{HttpPostHandler, ServerHttpCodecs}

class AgentServicePostHandler(agentService: AgentServiceApi, securityDirective: SecurityDirectives)
    extends HttpPostHandler[AgentServiceRequest]
    with ServerHttpCodecs {

  import agentService._
  override def handle(request: AgentServiceRequest): Route =
    request match {
      case SpawnSequenceComponent(agentPrefix, componentName, version) =>
        sPost(complete(spawnSequenceComponent(agentPrefix, componentName, version)))

      case SpawnSequenceManager(agentPrefix, obsModeConfigPath, isConfigLocal, version) =>
        sPost(complete(spawnSequenceManager(agentPrefix, obsModeConfigPath, isConfigLocal, version)))

      case SpawnContainers(agentPrefix, hostConfigPath, isConfigLocal) =>
        sPost(complete(spawnContainers(agentPrefix, hostConfigPath, isConfigLocal)))

      case KillComponent(componentId) =>
        sPost(complete(killComponent(componentId)))

      case GetAgentStatus => complete(getAgentStatus)
    }

  private def sPost(route: => Route): Route = securityDirective.sPost(AuthPolicies.eswUserRolePolicy)(_ => route)
}
