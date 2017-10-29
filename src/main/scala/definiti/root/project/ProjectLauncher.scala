package definiti.root.project

import java.nio.file.Paths

import akka.actor.ActorSystem
import definiti.root.config.Configuration

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.sys.process.Process

class ProjectLauncher(configuration: Configuration)(implicit actorSystem: ActorSystem) {
  import definiti.root.project.ProjectLauncher._

  implicit val executionContext: ExecutionContextExecutor = actorSystem.dispatcher

  def launch(): Future[Unit] = Future {
    val binPath = configuration.workingDirectory.resolve(bin)
    Process(s"${binPath} -Dconfig.file=definiti.conf", Paths.get(".").toFile).!
  }
}

object ProjectLauncher {
  val bin = "project/target/universal/stage/bin/project"
}