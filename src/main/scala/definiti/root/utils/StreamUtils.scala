package definiti.root.utils

import akka.stream.scaladsl.ZipWith

object StreamUtils {
  def wait2() = ZipWith[Unit, Unit, Unit]((_, _) => Unit)
  def wait3() = ZipWith[Unit, Unit, Unit, Unit]((_, _, _) => Unit)
  def wait4() = ZipWith[Unit, Unit, Unit, Unit, Unit]((_, _, _, _) => Unit)
  def wait5() = ZipWith[Unit, Unit, Unit, Unit, Unit, Unit]((_, _, _, _, _) => Unit)
}
