package definiti.root.project

import java.io.File
import java.nio.file.{Files, Path, Paths}

import akka.actor.ActorSystem
import definiti.root.config.Configuration

import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.sys.process.Process

class ProjectLauncher(configuration: Configuration)(implicit actorSystem: ActorSystem) {
  implicit val executionContext: ExecutionContextExecutor = actorSystem.dispatcher

  def launch(): Future[Unit] = Future {
    val cp = jarFiles.mkString(File.pathSeparator)
    val configFile = "definiti.conf"
    val mainClass = "definiti.core.Boot"
    Process(s"java -cp ${cp} -Dconfig.file=${configFile} ${mainClass}", Paths.get(".").toFile).!
  }

  private def jarFiles: Seq[String] = {
    val files = new ListBuffer[String]

    def process(path: Path): Unit = {
      if (Files.isDirectory(path)) {
        Files.list(path).forEach(process)
      } else if (path.toString.endsWith(".jar")) {
        files.append(path.toString)
      }
    }

    process(configuration.jarDependenciesDirectory)
    files
  }
}