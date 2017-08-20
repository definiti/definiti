package definiti.root.project

import akka.actor.ActorSystem
import definiti.root.config.Configuration

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.sys.process.Process

class ProjectLauncher(configuration: Configuration)(implicit actorSystem: ActorSystem) {
  import ProjectBuilder._

  implicit val executionContext: ExecutionContextExecutor = actorSystem.dispatcher

  def launch(): Future[Unit] = Future {
    val projectPath = configuration.workingDirectory.resolve(projectDirectory)
    val sbtPath = configuration.workingDirectory.resolve("sbt").resolve("bin").resolve("sbt.bat")
    Process(s"${sbtPath.toAbsolutePath} run", projectPath.toFile).!
  }
}

object ProjectLauncher {
  val projectDirectory = "project"
  val sbtDirectory = "sbt"
}