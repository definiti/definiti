package definiti.root

import java.nio.file.Paths

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.Logger
import definiti.root.config.Configuration

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}

object Boot {
  type Args = Array[String]
  val configurationKey = "config"

  val logger = Logger(Boot.getClass)

  def main(args: Args): Unit = {
    implicit val actorSystem: ActorSystem = ActorSystem()
    implicit val executionContext: ExecutionContextExecutor = actorSystem.dispatcher

    val config = ConfigFactory.parseFile(Paths.get("definiti.conf").toFile)
    val configuration = new Configuration(config)
    val projectProcessor = new ProjectProcessor(configuration)
    projectProcessor.run()
      .andThen {
        case Success(_) => logger.info("done")
        case Failure(error) => logger.error("An error happened during compilation", error)
      }
      .flatMap(_ => actorSystem.terminate())
      .foreach(_ => System.exit(0))
  }
}
