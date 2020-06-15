package esw.sm.api

import akka.actor.testkit.typed.scaladsl.TestProbe
import akka.actor.typed.{ActorSystem, SpawnProtocol}
import akka.serialization.SerializationExtension
import csw.location.api.models.ComponentId
import csw.location.api.models.ComponentType.Sequencer
import csw.prefix.models.Prefix
import csw.prefix.models.Subsystem.ESW
import esw.sm.api.actor.messages.SequenceManagerMsg._
import esw.sm.api.protocol.CommonFailure.{ConfigurationMissing, LocationServiceError}
import esw.sm.api.protocol._
import esw.sm.api.protocol.AgentError.SpawnSequenceComponentFailed
import esw.sm.api.protocol.StartSequencerResponse.LoadScriptError
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks.forAll
import org.scalatest.prop.Tables.Table
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.Await
import scala.concurrent.duration.DurationLong

class SmAkkaSerializerTest extends AnyWordSpecLike with Matchers with TypeCheckedTripleEquals with BeforeAndAfterAll {
  private final implicit val system: ActorSystem[SpawnProtocol.Command] = ActorSystem(SpawnProtocol(), "SmAkkaSerializerTest")
  private final val serialization                                       = SerializationExtension(system)

  override protected def afterAll(): Unit = {
    system.terminate()
    Await.result(system.whenTerminated, 2.seconds)
  }

  "should use sm serializer for SequenceManagerRemoteMsg (de)serialization" in {
    val configureResponseRef         = TestProbe[ConfigureResponse]().ref
    val getRunningModesResponseRef   = TestProbe[GetRunningObsModesResponse]().ref
    val cleanupResponseRef           = TestProbe[CleanupResponse]().ref
    val getSmStateRef                = TestProbe[SequenceManagerState]().ref
    val shutdownSequencerResponseRef = TestProbe[ShutdownSequencerResponse]().ref
    val StartSequencerResponseRef    = TestProbe[StartSequencerResponse]().ref

    val obsMode = "IRIS_Darknight"

    val testData = Table(
      "SequenceManagerRemoteMsg models",
      Configure(obsMode, configureResponseRef),
      Cleanup(obsMode, cleanupResponseRef),
      GetRunningObsModes(getRunningModesResponseRef),
      GetSequenceManagerState(getSmStateRef),
      StartSequencer(ESW, obsMode, StartSequencerResponseRef),
      ShutdownSequencer(ESW, obsMode, shutdownSequenceComp = false, shutdownSequencerResponseRef)
    )

    forAll(testData) { sequenceManagerRemoteMsg =>
      val serializer = serialization.findSerializerFor(sequenceManagerRemoteMsg)
      serializer.getClass shouldBe classOf[SmAkkaSerializer]

      val bytes = serializer.toBinary(sequenceManagerRemoteMsg)
      serializer.fromBinary(bytes, Some(sequenceManagerRemoteMsg.getClass)) shouldEqual sequenceManagerRemoteMsg
    }
  }

  "should use sm serializer for ConfigureResponse (de)serialization" in {
    val obsMode1 = "IRIS_Darknight"
    val obsMode2 = "IRIS_ClearSkies"

    val testData = Table(
      "Sequence Manager ConfigureResponse models",
      ConfigureResponse.Success(ComponentId(Prefix(ESW, "primary"), Sequencer)),
      ConfigureResponse.FailedToStartSequencers(Set("Error1", "Error2")),
      ConfigureResponse.ConflictingResourcesWithRunningObsMode(Set(obsMode1, obsMode2)),
      LocationServiceError("error"),
      ConfigurationMissing(obsMode1)
    )

    forAll(testData) { configureResponse =>
      val serializer = serialization.findSerializerFor(configureResponse)
      serializer.getClass shouldBe classOf[SmAkkaSerializer]

      val bytes = serializer.toBinary(configureResponse)
      serializer.fromBinary(bytes, Some(configureResponse.getClass)) shouldEqual configureResponse
    }
  }

  "should use sm serializer for CleanupResponse (de)serialization" in {
    val obsMode1 = "IRIS_Darknight"

    val testData = Table(
      "Sequence Manager CleanupResponse models",
      CleanupResponse.Success,
      LocationServiceError("error"),
      ConfigurationMissing(obsMode1)
    )

    forAll(testData) { cleanupResponse =>
      val serializer = serialization.findSerializerFor(cleanupResponse)
      serializer.getClass shouldBe classOf[SmAkkaSerializer]

      val bytes = serializer.toBinary(cleanupResponse)
      serializer.fromBinary(bytes, Some(cleanupResponse.getClass)) shouldEqual cleanupResponse
    }
  }

  "should use sm serializer for GetRunningObsModesResponse (de)serialization" in {
    val obsMode1 = "IRIS_Darknight"
    val obsMode2 = "IRIS_ClearSkies"

    val testData = Table(
      "Sequence Manager GetRunningObsModesResponse models",
      GetRunningObsModesResponse.Success(Set(obsMode1, obsMode2)),
      GetRunningObsModesResponse.Failed("error")
    )

    forAll(testData) { getRunningObsModesResponse =>
      val serializer = serialization.findSerializerFor(getRunningObsModesResponse)
      serializer.getClass shouldBe classOf[SmAkkaSerializer]

      val bytes = serializer.toBinary(getRunningObsModesResponse)
      serializer.fromBinary(bytes, Some(getRunningObsModesResponse.getClass)) shouldEqual getRunningObsModesResponse
    }
  }

  "should use sm serializer for StartSequencerResponse (de)serialization" in {
    val componentId = ComponentId(Prefix("IRIS.darknight"), Sequencer)
    val testData = Table(
      "Sequence Manager StartSequencerResponse models",
      StartSequencerResponse.Started(componentId),
      StartSequencerResponse.AlreadyRunning(componentId),
      LoadScriptError("error"),
      LocationServiceError("error"),
      SpawnSequenceComponentFailed("error")
    )

    forAll(testData) { startSequencerResponse =>
      val serializer = serialization.findSerializerFor(startSequencerResponse)
      serializer.getClass shouldBe classOf[SmAkkaSerializer]

      val bytes = serializer.toBinary(startSequencerResponse)
      serializer.fromBinary(bytes, Some(startSequencerResponse.getClass)) shouldEqual startSequencerResponse
    }
  }

  "should use sm serializer for ShutdownSequencerResponse (de)serialization" in {
    val testData = Table(
      "Sequence Manager ShutdownSequencerResponse models",
      ShutdownSequencerResponse.Success,
      LocationServiceError("error")
    )

    forAll(testData) { shutdownSequencerResponse =>
      val serializer = serialization.findSerializerFor(shutdownSequencerResponse)
      serializer.getClass shouldBe classOf[SmAkkaSerializer]

      val bytes = serializer.toBinary(shutdownSequencerResponse)
      serializer.fromBinary(bytes, Some(shutdownSequencerResponse.getClass)) shouldEqual shutdownSequencerResponse
    }
  }

  "should use sm serializer for SequenceManagerState (de)serialization" in {
    val testData = Table(
      "Sequence Manager SequenceManagerState models",
      SequenceManagerState.Idle,
      SequenceManagerState.CleaningUp,
      SequenceManagerState.Configuring
    )

    forAll(testData) { sequenceManagerState =>
      val serializer = serialization.findSerializerFor(sequenceManagerState)
      serializer.getClass shouldBe classOf[SmAkkaSerializer]

      val bytes = serializer.toBinary(sequenceManagerState)
      serializer.fromBinary(bytes, Some(sequenceManagerState.getClass)) shouldEqual sequenceManagerState
    }
  }
}
