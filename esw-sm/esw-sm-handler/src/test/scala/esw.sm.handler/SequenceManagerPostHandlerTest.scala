package esw.sm.handler

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import csw.location.api.models.{ComponentId, ComponentType}
import csw.prefix.models.Prefix
import csw.prefix.models.Subsystem.ESW
import esw.commons.BaseTestSuite
import esw.sm.api.SequenceManagerApi
import esw.sm.api.codecs.SequenceManagerHttpCodec
import esw.sm.api.protocol.SequenceManagerPostRequest._
import esw.sm.api.protocol._
import msocket.api.ContentType
import msocket.impl.post.{ClientHttpCodecs, PostRouteFactory}

import scala.concurrent.Future

class SequenceManagerPostHandlerTest
    extends BaseTestSuite
    with ScalatestRouteTest
    with SequenceManagerHttpCodec
    with ClientHttpCodecs {
  private val sequenceManagerApi: SequenceManagerApi = mock[SequenceManagerApi]
  private val postHandler                            = new SequenceManagerPostHandler(sequenceManagerApi)
  lazy val route: Route                              = new PostRouteFactory[SequenceManagerPostRequest]("post-endpoint", postHandler).make()
  private val obsMode                                = "IRIS_darknight"
  private val componentId                            = ComponentId(Prefix(ESW, obsMode), ComponentType.Sequencer)

  override def clientContentType: ContentType = ContentType.Json

  implicit class Narrower(x: SequenceManagerPostRequest) {
    def narrow: SequenceManagerPostRequest = x
  }

  "SequenceManagerPostHandler" must {
    "return running observation modes for getRunningObsModes request | ESW-171" in {
      val obsModes = Set(obsMode)
      when(sequenceManagerApi.getRunningObsModes).thenReturn(Future.successful(GetRunningObsModesResponse.Success(obsModes)))

      Post("/post-endpoint", GetRunningObsModes.narrow) ~> route ~> check {
        verify(sequenceManagerApi).getRunningObsModes
        responseAs[GetRunningObsModesResponse] should ===(GetRunningObsModesResponse.Success(obsModes))
      }
    }

    "return cleanup success for cleanup request | ESW-171" in {
      when(sequenceManagerApi.cleanup(obsMode)).thenReturn(Future.successful(CleanupResponse.Success))

      Post("/post-endpoint", Cleanup(obsMode).narrow) ~> route ~> check {
        verify(sequenceManagerApi).cleanup(obsMode)
        responseAs[CleanupResponse] should ===(CleanupResponse.Success)
      }
    }

    "return start sequencer success for startSequencer request | ESW-171" in {
      when(sequenceManagerApi.startSequencer(ESW, obsMode))
        .thenReturn(Future.successful(StartSequencerResponse.Started(componentId)))

      Post("/post-endpoint", StartSequencer(ESW, obsMode).narrow) ~> route ~> check {
        verify(sequenceManagerApi).startSequencer(ESW, obsMode)
        responseAs[StartSequencerResponse] should ===(StartSequencerResponse.Started(componentId))
      }
    }

    "return shutdown sequencer success for shutdownSequencer request | ESW-171" in {
      when(sequenceManagerApi.shutdownSequencer(ESW, obsMode))
        .thenReturn(Future.successful(ShutdownSequencerResponse.Success))

      Post("/post-endpoint", ShutdownSequencer(ESW, obsMode, shutdownSequenceComp = false).narrow) ~> route ~> check {
        verify(sequenceManagerApi).shutdownSequencer(ESW, obsMode)
        responseAs[ShutdownSequencerResponse] should ===(ShutdownSequencerResponse.Success)
      }
    }

    "return restart sequencer success for restartSequencer request | ESW-171" in {
      when(sequenceManagerApi.restartSequencer(ESW, obsMode))
        .thenReturn(Future.successful(RestartSequencerResponse.Success(componentId)))

      Post("/post-endpoint", RestartSequencer(ESW, obsMode).narrow) ~> route ~> check {
        verify(sequenceManagerApi).restartSequencer(ESW, obsMode)
        responseAs[RestartSequencerResponse] should ===(RestartSequencerResponse.Success(componentId))
      }
    }
  }
}
