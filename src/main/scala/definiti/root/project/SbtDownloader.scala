package definiti.root.project

import java.nio.file.{Files, Path}

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{HttpMethods, HttpRequest}
import better.files.File
import definiti.root.config.Configuration
import definiti.root.utils.Http

import scala.concurrent.{ExecutionContextExecutor, Future}

class SbtDownloader(configuration: Configuration)(implicit actorSystem: ActorSystem) {
  import SbtDownloader._

  private implicit val executionContext: ExecutionContextExecutor = actorSystem.dispatcher

  private val http = new Http()

  def load(): Future[Unit] = {
    if (Files.notExists(configuration.workingDirectory.resolve(sbtDirectory))) {
      download()
    } else {
      Future.successful()
    }
  }

  private def download(): Future[Unit] = {
    val downloadPath = configuration.workingDirectory.resolve(temporaryZipFile)
    val downloadFile = File(downloadPath)
    downloadZip(downloadPath)
      .map(_ => downloadFile.unzipTo(configuration.workingDirectory))
      .map(_ => downloadFile.delete())
  }

  private def downloadZip(destinationFile: Path): Future[Unit] = {
    http.binaryRequest(HttpRequest(
      method = HttpMethods.GET,
      uri = sbtUrl
    ), destinationFile)
  }
}

object SbtDownloader {
  val version = "1.0.0"

  val sbtUrl = s"https://github.com/sbt/sbt/releases/download/v${version}/sbt-${version}.zip"

  val temporaryZipFile = s"_sbt-${version}.zip"

  val projectDirectory = "project"

  val sbtDirectory = "sbt"
}