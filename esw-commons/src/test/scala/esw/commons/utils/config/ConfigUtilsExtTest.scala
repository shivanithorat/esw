package esw.commons.utils.config

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.ActorSystem
import com.typesafe.config.{Config, ConfigException, ConfigOrigin}
import csw.config.api.exceptions.FileNotFound
import csw.config.client.commons.ConfigUtils
import esw.testcommons.BaseTestSuite
import org.scalatest.prop.Tables.Table

import java.nio.file.Path
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContextExecutor, Future}

class ConfigUtilsExtTest extends BaseTestSuite {
  val actorTestKit: ActorTestKit            = ActorTestKit()
  val actorSystem: ActorSystem[_]           = actorTestKit.system
  implicit val ec: ExecutionContextExecutor = actorSystem.executionContext

  "findVersion" must {
    val errorMsg        = randomString(10)
    val versionConfPath = Path.of(randomString(10))

    "return version if config is present | ESW-360" in {
      val configUtils    = mock[ConfigUtils]
      val configUtilsExt = new ConfigUtilsExt(configUtils)
      val config         = mock[Config]
      val version        = randomString(10)

      when(configUtils.getConfig(versionConfPath, isLocal = false)).thenReturn(Future.successful(config))
      when(config.getString("scripts.version")).thenReturn(version)
      configUtilsExt.findVersion(versionConfPath).futureValue should ===(version)
    }

    Table(
      ("exception", "expectedMsg"),
      (FileNotFound(errorMsg), errorMsg),
      (new ConfigException.Missing(versionConfPath.toString), "scripts.version is not present"),
      (new ConfigException.WrongType(mock[ConfigOrigin], versionConfPath.toString), "value of scripts.version is not string")
    ).foreach {
      case (exception, msg) =>
        s"throw ScriptVersionConfException if ${exception.getClass.getSimpleName} | ESW-360" in {
          val configUtils    = mock[ConfigUtils]
          val configUtilsExt = new ConfigUtilsExt(configUtils)

          when(configUtils.getConfig(versionConfPath, isLocal = false)).thenReturn(Future.failed(exception))

          val scriptVersionConfException = intercept[ScriptVersionConfException] {
            Await.result(configUtilsExt.findVersion(versionConfPath), 100.millis)
          }
          scriptVersionConfException should ===(ScriptVersionConfException(msg))
        }
    }
  }

  override protected def afterAll(): Unit = {
    actorTestKit.shutdownTestKit()
    super.afterAll()
  }
}
