package esw.performance.utils

object Timing {

  def measureTimeMillis[R](block: => R): (R, Long) = {
    val t0          = System.nanoTime()
    val result      = block
    val t1          = System.nanoTime()
    val elapsedTime = (t1 - t0) / 1000000 // converted to milli-seconds
    (result, elapsedTime)
  }

}
