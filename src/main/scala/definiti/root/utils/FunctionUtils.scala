package definiti.root.utils

import scala.concurrent.{ExecutionContext, Future}

object FunctionUtils {
  def withIdentity[A](operation: A => Unit)(value: A): A = {
    operation(value)
    value
  }

  def withFutureIdentity[A](operation: A => Future[Unit])(value: A)(implicit executionContext: ExecutionContext): Future[A] = {
    operation(value).map(_ => value)
  }
}
