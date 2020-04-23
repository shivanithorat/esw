package esw.commons.utils.location

sealed trait EswLocationError extends Throwable {
  def msg: String
}

object EswLocationError {
  case class ResolveLocationFailed(msg: String)     extends EswLocationError
  case class RegistrationListingFailed(msg: String) extends EswLocationError
}