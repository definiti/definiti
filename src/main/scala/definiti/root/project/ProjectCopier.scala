package definiti.root.project

import akka.actor.ActorSystem
import better.files.File
import definiti.root.config.Configuration

import scala.concurrent.{ExecutionContextExecutor, Future}

class ProjectCopier(configuration: Configuration)(
  implicit actorSystem: ActorSystem
) {
  implicit val executionContext: ExecutionContextExecutor = actorSystem.dispatcher

  def copyProject(): Future[Unit] = Future[Unit] {
    copySrc()
    copyConf()
  }

  def copySrc(): Unit = {
    val destination = File(configuration.projectDirectory.resolve("src").resolve("main").resolve("definiti"))
    if (destination.exists) {
      destination.delete()
    }
    destination.parent.createDirectories()
    File(configuration.sourceDirectory).copyTo(destination)
  }

  def copyConf(): Unit = {
    val destination = File(configuration.projectDirectory.resolve("src").resolve("main").resolve("resources").resolve("application.conf"))
    if (destination.exists) {
      destination.delete()
    }
    destination.parent.createDirectories()
    File(configuration.confFile).copyTo(destination)
  }
}
