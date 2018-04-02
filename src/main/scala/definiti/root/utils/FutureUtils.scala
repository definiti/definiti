package definiti.root.utils

import akka.NotUsed
import akka.stream.scaladsl.Flow
import com.typesafe.scalalogging.Logger

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.Failure

object FutureUtils {

  implicit class FutureExtension[A](future: Future[A]) {
    def toFlow: Flow[Unit, A, NotUsed] = FutureUtils.toFlow(future)

    def logError(implicit logger: Logger, executionContext: ExecutionContext): Future[A] = FutureUtils.logError(future)
  }

  def toFlow[A](future: Future[A]): Flow[Unit, A, NotUsed] = {
    Flow[Unit].mapAsync(1)(_ => future)
  }

  def logError[A](future: Future[A])(implicit logger: Logger, executionContext: ExecutionContext): Future[A] = {
    future.andThen {
      case Failure(error) => logger.error("Exception in future", error)
      case _ =>
    }
  }

  implicit def taskToFuture[A](task: scalaz.concurrent.Task[A]): Future[A] = {
    import scalaz.{-\/, \/-}

    val p: Promise[A] = Promise()
    task.unsafePerformAsync {
      case -\/(ex) => p.failure(ex)
      case \/-(r) => p.success(r)
    }
    p.future
  }

  def unit[A](a: A): Unit = ()
}
