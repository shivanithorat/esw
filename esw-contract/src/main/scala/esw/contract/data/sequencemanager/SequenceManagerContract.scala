package esw.contract.data.sequencemanager

import csw.contract.ResourceFetcher
import csw.contract.generator.ClassNameHelpers._
import csw.contract.generator._
import csw.prefix.models.Subsystem
import esw.sm.api.codecs.SequenceManagerHttpCodec
import esw.sm.api.protocol.SequenceManagerPostRequest._
import esw.sm.api.protocol._

object SequenceManagerContract extends SequenceManagerHttpCodec with SequenceManagerData {

  private val models: ModelSet = ModelSet.models(
    ModelType(sequencerPrefix),
    ModelType(obsMode),
    ModelType(componentId),
    ModelType(Subsystem),
    ModelType(sequenceComponentStatus),
    ModelType(agentSeqCompsStatus),
    ModelType(akkaLocation),
    ModelType(provisionConfig),
    ModelType(agentProvisionConfig)
  )

  private val httpRequests = new RequestSet[SequenceManagerPostRequest] {
    requestType(configure)
    requestType(provision)
    requestType(getRunningObsModes)
    requestType(startSequencer)
    requestType(restartSequencer)
    requestType(shutdownSequencer)
    requestType(shutdownSubsystemSequencers)
    requestType(shutdownObsModeSequencers)
    requestType(shutdownAllSequencers)
    requestType(spawnSequenceComponent)
    requestType(shutdownSequenceComponent)
    requestType(shutdownAllSequenceComponents)
    requestType(getAgentStatus)
  }

  private val httpEndpoints: List[Endpoint] = List(
    Endpoint(name[Configure], name[ConfigureResponse]),
    Endpoint(name[Provision], name[ProvisionResponse]),
    Endpoint(objectName(GetRunningObsModes), name[GetRunningObsModesResponse]),
    Endpoint(name[StartSequencer], name[StartSequencerResponse]),
    Endpoint(name[RestartSequencer], name[RestartSequencerResponse]),
    Endpoint(name[ShutdownSequencer], name[ShutdownSequencersResponse]),
    Endpoint(name[ShutdownSubsystemSequencers], name[ShutdownSequencersResponse]),
    Endpoint(name[ShutdownObsModeSequencers], name[ShutdownSequencersResponse]),
    Endpoint(objectName(ShutdownAllSequencers), name[ShutdownSequencersResponse]),
    Endpoint(name[ShutdownSequenceComponent], name[ShutdownSequenceComponentResponse]),
    Endpoint(objectName(ShutdownAllSequenceComponents), name[ShutdownSequenceComponentResponse]),
    Endpoint(objectName(GetAgentStatus), name[AgentStatusResponse])
  )

  private val readme: Readme = Readme(ResourceFetcher.getResourceAsString("sequence-manager-service/README.md"))

  val service: Service = Service(
    `http-contract` = Contract(httpEndpoints, httpRequests),
    `websocket-contract` = Contract.empty,
    models = models,
    readme = readme
  )
}