package esw.agent.akka.app.process.cs

import esw.agent.akka.client.models.ContainerConfig

object Coursier {
  lazy val cs: String = "cs"

  def ocsApp(version: Option[String]): CoursierLaunch = CoursierLaunch("ocs-app", version)

  def smApp(version: Option[String]): CoursierLaunch = CoursierLaunch("sequence-manager", version)

  def containerApp(config: ContainerConfig): CoursierLaunch = {
    val appName = s"${config.orgName}::${config.deployModule}"
    CoursierLaunch(appName, Some(config.version))
  }
}
