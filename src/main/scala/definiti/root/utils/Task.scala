package definiti.root.utils

import akka.NotUsed
import akka.stream.scaladsl.Flow
import com.typesafe.scalalogging.Logger
import definiti.root.utils.Dummies._
import FutureUtils._

import scala.concurrent.{ExecutionContext, Future}

trait Task[Input, Output] {
  def toFlow(implicit logger: Logger): Flow[Input, Output, NotUsed] = {
    Flow[Input]
      .map { input =>
        logger.info(s"$name start")
        input
      }
      .via(createFlow())
      .map { output =>
        logger.info(s"$name end")
        output
      }
  }

  protected def name: String

  protected def createFlow(): Flow[Input, Output, NotUsed]
}

object Task {
  def apply[Output](name: String, process: () => Output): Task[Unit, Output] = {
    new IndependentTask[Output](name, process)
  }

  def apply[Output](name: String, process: => Output)(implicit dummyImplicit: DummyImplicit): Task[Unit, Output] = {
    new IndependentTask[Output](name, () => process)
  }

  def apply[Output](name: String, process: () => Future[Output])(implicit dummyImplicit: DummyImplicit2, logger: Logger, executionContext: ExecutionContext): Task[Unit, Output] = {
    new IndependentFutureTask[Output](name, () => process().logError)
  }

  def apply[Output](name: String, process: => Future[Output])(implicit dummyImplicit: DummyImplicit3, logger: Logger, executionContext: ExecutionContext): Task[Unit, Output] = {
    new IndependentFutureTask[Output](name, () => process.logError)
  }

  def apply[Input, Output](name: String, process: Input => Output): Task[Input, Output] = {
    new DependentTask[Input, Output](name, process)
  }

  def apply[Input, Output](name: String, process: Input => Future[Output])(implicit dummyImplicit: DummyImplicit, logger: Logger, executionContext: ExecutionContext): Task[Input, Output] = {
    new DependentFutureTask[Input, Output](name, process(_).logError)
  }
}

private[utils] case class IndependentTask[Output](name: String, process: () => Output) extends Task[Unit, Output] {
  override protected def createFlow() = Flow[Unit].map(_ => process())
}

private[utils] case class IndependentFutureTask[Output](name: String, process: () => Future[Output]) extends Task[Unit, Output] {
  override protected def createFlow() = Flow[Unit].mapAsync(1)(_ => process())
}

private[utils] case class DependentTask[Input, Output](name: String, process: Input => Output) extends Task[Input, Output] {
  override protected def createFlow() = Flow[Input].map(process)
}

private[utils] case class DependentFutureTask[Input, Output](name: String, process: Input => Future[Output]) extends Task[Input, Output] {
  override protected def createFlow() = Flow[Input].mapAsync(1)(process)
}