package esw.gateway.server.routes

import akka.http.scaladsl.marshalling.sse.EventStreamMarshalling._
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.http.scaladsl.server.Directives.{entity, _}
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.Source
import akka.util.Timeout
import csw.location.api.models.ComponentType
import csw.params.commands.ControlCommand
import csw.params.core.models.Id
import csw.params.core.states.StateName
import esw.template.http.server.commons.JsonSupportExt
import esw.template.http.server.csw.utils.CswContext
import play.api.libs.json.Json

import scala.concurrent.duration.DurationLong

class CommandRoutes(cswCtx: CswContext) extends JsonSupportExt {
  import cswCtx._
  import actorRuntime._

  def commandRoutes(componentType: String): Route =
    pathPrefix("command") {
      pathPrefix(componentType / Segment) { componentName =>
        val commandServiceF = componentFactory.commandService(componentName, ComponentType.withName(componentType))

        onSuccess(commandServiceF) { commandService =>
          post {
            path("validate") {
              entity(as[ControlCommand]) { command =>
                complete(commandService.validate(command))
              }
            } ~
            path("submit") {
              entity(as[ControlCommand]) { command =>
                complete(commandService.submit(command))
              }
            } ~
            path("oneway") {
              entity(as[ControlCommand]) { command =>
                complete(commandService.oneway(command))
              }
            }
          } ~
          get {
            path(Segment) { runId =>
              val responseF = commandService.queryFinal(Id(runId))(Timeout(100.hours))
              onSuccess(responseF) { response =>
                complete {
                  Source
                    .single(ServerSentEvent(Json.toJson(response).toString()))
                    .keepAlive(1.second, () => ServerSentEvent.heartbeat)
                }
              }
            } ~
            path("current-state" / "subscribe") {
              parameters('stateName.*) { stateNames =>
                complete {
                  commandService
                    .subscribeCurrentState(stateNames.map(StateName.apply).toSet)
                    .map(state => ServerSentEvent(Json.toJson(state).toString()))
                    .keepAlive(1.second, () => ServerSentEvent.heartbeat)
                }
              }
            }
          }
        }
      }
    }

  val route: Route = commandRoutes("hcd") ~ commandRoutes("assembly")
}
